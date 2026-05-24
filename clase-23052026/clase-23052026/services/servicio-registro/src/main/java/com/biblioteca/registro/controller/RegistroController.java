package com.biblioteca.registro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * =================================================================
 * CONCEPTO SOA — Service Registry / Service Inventory
 * =================================================================
 *
 * En SOA clásico, UDDI (Universal Description, Discovery and Integration)
 * era el directorio donde los servicios se registraban y los consumidores
 * los descubrían.
 *
 * UDDI estaba compuesto por tres "libros":
 *   - White Pages:  ¿quién ofrece el servicio? (nombre, contacto)
 *   - Yellow Pages: ¿qué tipo de servicio es? (categoría)
 *   - Green Pages:  ¿cómo se usa? (contrato técnico, WSDL)
 *
 * Este servicio es el equivalente moderno:
 *   - Lista todos los servicios del ecosistema
 *   - Verifica su estado en tiempo real
 *   - Enlaza al contrato (OpenAPI/WSDL)
 *
 * En producción, esta función la cumple:
 *   - Consul, Eureka (Spring Cloud) o etcd para microservicios
 *   - API Management (Azure, AWS, Kong) para APIs
 *   - El propio ESB (MuleSoft, WSO2) para SOA clásico
 * =================================================================
 */
@RestController
@RequestMapping("/registro")
@Tag(name = "Service Registry", description = "Inventario de servicios SOA — equivalente moderno de UDDI (Sesiones 10-12)")
public class RegistroController {

    // URLs internas — para llamadas entre contenedores (Docker DNS: soa-libros, soa-usuarios…)
    @Value("${libros.service.url:http://localhost:8001}")
    private String librosUrl;

    @Value("${usuarios.service.url:http://localhost:8002}")
    private String usuariosUrl;

    @Value("${prestamos.service.url:http://localhost:8003}")
    private String prestamosUrl;

    @Value("${soap.service.url:http://localhost:8004/ws}")
    private String soapUrl;

    @Value("${xml.service.url:http://localhost:8005}")
    private String xmlUrl;

    // URLs externas — se muestran al cliente (siempre localhost, accesibles desde el navegador)
    @Value("${libros.display.url:http://localhost:8001}")
    private String librosDisplayUrl;

    @Value("${usuarios.display.url:http://localhost:8002}")
    private String usuariosDisplayUrl;

    @Value("${prestamos.display.url:http://localhost:8003}")
    private String prestamosDisplayUrl;

    @Value("${soap.display.url:http://localhost:8004/ws}")
    private String soapDisplayUrl;

    @Value("${xml.display.url:http://localhost:8005}")
    private String xmlDisplayUrl;

    @Value("${registro.display.url:http://localhost:8007}")
    private String registroDisplayUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Inventario completo ───────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Inventario completo del ecosistema SOA",
        description = """
            Retorna la lista de todos los servicios registrados con:
            - Tipo de servicio (ENTITY, TASK, SOAP, UTILITY)
            - Estado en tiempo real (DISPONIBLE / NO_DISPONIBLE)
            - Contrato del servicio (OpenAPI JSON o WSDL)
            - Operaciones disponibles

            CONCEPTO UDDI:
              White Pages  → nombre y descripción del servicio
              Yellow Pages → tipo (ENTITY, TASK, SOAP…)
              Green Pages  → swagger/contrato + operaciones

            En SOA empresarial: Consul, Eureka, Azure API Management.
            """
    )
    public Map<String, Object> inventario() {
        return Map.of(
            "sistema",         "Biblioteca SOA — Universidad",
            "version",         "1.0.0",
            "timestamp",       LocalDateTime.now().toString(),
            "total_servicios", 5,
            "swagger_registro", registroDisplayUrl + "/swagger-ui.html",
            "servicios",       buildInventario()
        );
    }

    @GetMapping("/salud")
    @Operation(
        summary = "Estado de salud de todos los servicios",
        description = "Verifica el estado de cada servicio en tiempo real. Útil para monitoreo del ecosistema."
    )
    public Map<String, Object> saludEcosistema() {
        var libros    = verificarEstado(librosUrl + "/libros/health");
        var usuarios  = verificarEstado(usuariosUrl + "/usuarios/health");
        var prestamos = verificarEstado(prestamosUrl + "/prestamos/health");
        var soap      = verificarEstado(soapUrl + "/libros.wsdl");
        var xml       = verificarEstado(xmlUrl + "/xml/catalogo");

        long disponibles = List.of(libros, usuarios, prestamos, soap, xml)
                              .stream().filter("DISPONIBLE"::equals).count();

        return Map.of(
            "ecosistema",        disponibles == 5 ? "SALUDABLE" : "DEGRADADO",
            "servicios_activos", disponibles + "/5",
            "detalle", Map.of(
                "servicio-libros",      libros,
                "servicio-usuarios",    usuarios,
                "servicio-prestamos",   prestamos,
                "servicio-libros-soap", soap,
                "servicio-xml",         xml
            )
        );
    }

    @GetMapping("/health")
    @Operation(summary = "Estado del propio registro")
    public Map<String, String> health() {
        return Map.of("servicio", "registro", "estado", "activo", "version", "1.0.0");
    }

    // ── Construcción del inventario ───────────────────────────────

    private List<Map<String, Object>> buildInventario() {
        return List.of(
            servicio(
                "Libros Entity Service",
                "ENTITY",
                librosDisplayUrl,
                librosUrl + "/libros/health",           // health: URL interna (Docker)
                librosDisplayUrl + "/swagger-ui.html",  // swagger: URL externa (localhost)
                librosDisplayUrl + "/api-docs",
                "Gestión del catálogo de libros. Dato maestro del ecosistema. Puerto 8001.",
                new String[]{
                    "GET  /libros                          — listar todos",
                    "GET  /libros/{id}                     — obtener por ID",
                    "POST /libros                          — registrar libro",
                    "PATCH /libros/{id}/disponibilidad     — cambiar disponibilidad",
                    "DELETE /libros/{id}                   — eliminar"
                }
            ),
            servicio(
                "Usuarios Entity Service",
                "ENTITY",
                usuariosDisplayUrl,
                usuariosUrl + "/usuarios/health",
                usuariosDisplayUrl + "/swagger-ui.html",
                usuariosDisplayUrl + "/api-docs",
                "Gestión de usuarios registrados. Soft delete para trazabilidad. Puerto 8002.",
                new String[]{
                    "GET  /usuarios                        — listar todos",
                    "GET  /usuarios/{id}                   — obtener por ID",
                    "POST /usuarios                        — registrar usuario",
                    "PATCH /usuarios/{id}/estado           — activar/desactivar"
                }
            ),
            servicio(
                "Préstamos Task Service",
                "TASK",
                prestamosDisplayUrl,
                prestamosUrl + "/prestamos/health",
                prestamosDisplayUrl + "/swagger-ui.html",
                prestamosDisplayUrl + "/api-docs",
                "Orquestación de préstamos. Compone Libros + Usuarios. Comunicación síncrona vía RestTemplate. Puerto 8003.",
                new String[]{
                    "POST /prestamos                       — crear préstamo (orquesta 2 servicios)",
                    "GET  /prestamos                       — listar préstamos",
                    "GET  /prestamos/{id}                  — detalle préstamo",
                    "POST /prestamos/{id}/devolver         — registrar devolución"
                }
            ),
            servicio(
                "Libros SOAP Service",
                "SOAP",
                soapDisplayUrl,
                soapUrl + "/libros.wsdl",
                soapDisplayUrl + "/libros.wsdl",
                soapDisplayUrl + "/libros.wsdl",
                "SOA Clásico. Protocolo SOAP/XML. Contrato en WSDL. Contract-first con XSD. Puerto 8004.",
                new String[]{
                    "ListarLibros              — equivale a GET /libros",
                    "GetLibro (id)             — equivale a GET /libros/{id}",
                    "AgregarLibro              — equivale a POST /libros",
                    "CambiarDisponibilidad     — equivale a PATCH /libros/{id}/disponibilidad"
                }
            ),
            servicio(
                "XML Lab Service",
                "UTILITY",
                xmlDisplayUrl,
                xmlUrl + "/xml/catalogo",
                xmlDisplayUrl + "/swagger-ui.html",
                xmlDisplayUrl + "/api-docs",
                "Laboratorio educativo de tecnologías XML: XPath, XSLT, XSD. Puerto 8005.",
                new String[]{
                    "GET  /xml/catalogo                    — documento XML de ejemplo",
                    "POST /xml/validar                     — validación XSD",
                    "POST /xml/xpath?expresion=...         — ejecutar XPath",
                    "POST /xml/transformar/html            — XSLT → HTML",
                    "POST /xml/transformar/inventario      — XSLT → XML"
                }
            )
        );
    }

    private Map<String, Object> servicio(String nombre, String tipo, String urlBase,
                                          String healthUrl, String swaggerUrl, String contratoUrl,
                                          String descripcion, String[] operaciones) {
        return Map.of(
            "nombre",      nombre,
            "tipo",        tipo,
            "url_base",    urlBase,
            "estado",      verificarEstado(healthUrl),
            "swagger_ui",  swaggerUrl,
            "contrato",    contratoUrl,
            "descripcion", descripcion,
            "operaciones", List.of(operaciones)
        );
    }

    private String verificarEstado(String healthUrl) {
        try {
            // String.class activa StringHttpMessageConverter (Accept: */*),
            // aceptando application/xml (WSDL, catálogo) y application/json por igual.
            // Object.class solo activa Jackson (Accept: application/json) → 406 en endpoints XML.
            restTemplate.getForObject(healthUrl, String.class);
            return "DISPONIBLE";
        } catch (Exception e) {
            return "NO_DISPONIBLE";
        }
    }
}
