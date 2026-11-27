package com.example.Investigacion.facade;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Investigacion.common.ApiException;
import com.example.Investigacion.db.DatabaseService;
import com.example.Investigacion.db.Sql;
import com.example.Investigacion.dto.AsignarProfesorRequest;
import com.example.Investigacion.dto.CambioDirectorRequest;
import com.example.Investigacion.dto.CerrarProyectoRequest;
import com.example.Investigacion.dto.CrearUsuarioRequest;
import com.example.Investigacion.dto.ProductoRequest;
import com.example.Investigacion.dto.ProrrogaRequest;
import com.example.Investigacion.dto.RegistrarEstudianteRequest;
import com.example.Investigacion.dto.RegistrarProyectoRequest;
import com.example.Investigacion.dto.RelevoRequest;
import com.example.Investigacion.producto.Producto;
import com.example.Investigacion.producto.ProductoFactory;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionUser;

@Service
/*
 * BLOQUE: Fachada del sistema
 * Punto único de entrada para catálogos, transacciones, reportes, vistas, usuarios y auditoría. Aplica el aislamiento de datos por persona.
 */
public class GestionInvestigacionFacade {

    private final DatabaseService databaseService;
    private final ProductoFactory productoFactory;

    public GestionInvestigacionFacade(DatabaseService databaseService, ProductoFactory productoFactory) {
        this.databaseService = databaseService;
        this.productoFactory = productoFactory;
    }

    /*
     * Aislamiento por persona
     * Obtiene el id vinculado a la cuenta; si falta, deniega el acceso con HTTP 403.
     */
    private Integer idProfesorSesion(SessionUser usuario) {
        if (usuario.idProfesor() == null) {
            throw new ApiException(403, "La cuenta del docente no tiene una persona asignada.");
        }
        return usuario.idProfesor();
    }

    private Integer idEstudianteSesion(SessionUser usuario) {
        if (usuario.idEstudiante() == null) {
            throw new ApiException(403, "La cuenta del estudiante no tiene una persona asignada.");
        }
        return usuario.idEstudiante();
    }

    // ------------------------------------------------------------------
    // Catalogos de apoyo para el frontend (conexion privilegiada)
    // ------------------------------------------------------------------

    public List<Map<String, Object>> catalogoProyectos() {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT p.id_proyecto AS id, p.acronimo, p.nombre_proyecto AS nombre,
                       p.resolucion_consejo_uni_cierre AS cierre
                  FROM proyecto_investigacion p
                 ORDER BY p.acronimo
                """));
    }

    public List<Map<String, Object>> catalogoProfesores() {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT id_profesor AS id, nombre, departamento_facultad AS departamento
                  FROM profesores
                 ORDER BY nombre
                """));
    }

    public List<Map<String, Object>> catalogoEstudiantes() {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT e.id_estudiante AS id, e.nombres_est AS nombre,
                       e.telefono_est AS telefono, e.departamento_facultad AS departamento,
                       e.id_proyecto, p.acronimo
                  FROM estudiante e
                  JOIN proyecto_investigacion p ON p.id_proyecto = e.id_proyecto
                 ORDER BY e.nombres_est
                """));
    }

    public List<Map<String, Object>> catalogoLineas() {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT id_linea AS id, nombre_linea AS nombre, descriptores
                  FROM linea_investigacion
                 ORDER BY nombre_linea
                """));
    }

    public List<Map<String, Object>> catalogoTiposProducto() {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT id_tipo AS id, descripcion_tipo AS descripcion
                  FROM tipo_pro
                 ORDER BY id_tipo
                """));
    }

    public List<Map<String, Object>> equipoProyecto(Integer idProyecto) {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT pp.id_profesor, pf.nombre, pp.cargo,
                       pp.resolucion_ingreso, pp.resolucion_salida, pp.motivo_salida
                  FROM proyecto_profesor pp
                  JOIN profesores pf ON pf.id_profesor = pp.id_profesor
                 WHERE pp.id_proyecto = ?
                 ORDER BY (pp.resolucion_salida IS NOT NULL), pp.cargo, pf.nombre
                """, idProyecto));
    }

    public List<Map<String, Object>> estudiantesProyecto(Integer idProyecto) {
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT id_estudiante, nombres_est, telefono_est, departamento_facultad
                  FROM estudiante
                 WHERE id_proyecto = ?
                 ORDER BY nombres_est
                """, idProyecto));
    }

    public Map<String, Object> resumenDashboard() {
        return databaseService.conCatalogo(conexion -> {
            Map<String, Object> resumen = new java.util.LinkedHashMap<>();
            resumen.put("proyectos", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM proyecto_investigacion").get("total"));
            resumen.put("proyectosActivos", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM proyecto_investigacion WHERE resolucion_consejo_uni_cierre IS NULL")
                    .get("total"));
            resumen.put("profesores", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM profesores").get("total"));
            resumen.put("estudiantes", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM estudiante").get("total"));
            resumen.put("productos", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM productos").get("total"));
            resumen.put("lineas", Sql.consultarUno(conexion,
                    "SELECT COUNT(*) AS total FROM linea_investigacion").get("total"));
            return resumen;
        });
    }

    // ------------------------------------------------------------------
    // Proyectos (operaciones con la conexion del usuario autenticado)
    // ------------------------------------------------------------------

    public List<Map<String, Object>> listarProyectos(SessionUser usuario) {
        return databaseService.conUsuario(usuario, conexion -> Sql.consultar(conexion, """
                SELECT p.id_proyecto, p.nombre_proyecto, p.acronimo, p.presupuesto, p.programa,
                       p.descripcion, p.inicio_pro, p.fin_pro, p.prorroga,
                       p.resolucion_consejo_uni_aprobado, p.resolucion_consejo_uni_cierre,
                       COALESCE(r.total_productos, 0) AS total_productos,
                       (SELECT COUNT(*) FROM proyecto_profesor pp
                         WHERE pp.id_proyecto = p.id_proyecto AND pp.resolucion_salida IS NULL) AS docentes_activos,
                       (SELECT COUNT(*) FROM estudiante e
                         WHERE e.id_proyecto = p.id_proyecto) AS estudiantes,
                       CASE WHEN p.resolucion_consejo_uni_cierre IS NOT NULL THEN 'Cerrado'
                            WHEN COALESCE(p.prorroga, p.fin_pro) < CURDATE() THEN 'Vencido sin cierre'
                            WHEN COALESCE(p.prorroga, p.fin_pro) <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)
                                 THEN 'Por finalizar'
                            ELSE 'En ejecucion' END AS estado
                  FROM proyecto_investigacion p
                  LEFT JOIN resumen_proyecto r ON r.id_proyecto = p.id_proyecto
                 ORDER BY p.id_proyecto DESC
                """));
    }

    public Map<String, Object> detalleProyecto(SessionUser usuario, Integer idProyecto) {
        return databaseService.conUsuario(usuario, conexion -> {
            Map<String, Object> detalle = new java.util.LinkedHashMap<>();
            detalle.put("proyecto", Sql.consultarUno(conexion, """
                    SELECT p.*, COALESCE(r.total_productos, 0) AS total_productos
                      FROM proyecto_investigacion p
                      LEFT JOIN resumen_proyecto r ON r.id_proyecto = p.id_proyecto
                     WHERE p.id_proyecto = ?
                    """, idProyecto));
            detalle.put("equipo", Sql.consultar(conexion, """
                    SELECT pp.id_profesor, pf.nombre, pp.cargo, pp.num_profesores,
                           pp.resolucion_ingreso, pp.resolucion_salida, pp.motivo_salida
                      FROM proyecto_profesor pp
                      JOIN profesores pf ON pf.id_profesor = pp.id_profesor
                     WHERE pp.id_proyecto = ?
                     ORDER BY (pp.resolucion_salida IS NOT NULL), pp.cargo, pf.nombre
                    """, idProyecto));
            detalle.put("estudiantes", Sql.consultar(conexion, """
                    SELECT id_estudiante, nombres_est, telefono_est, departamento_facultad
                      FROM estudiante WHERE id_proyecto = ? ORDER BY nombres_est
                    """, idProyecto));
            detalle.put("productos", Sql.consultar(conexion, """
                    SELECT pr.id_producto, pr.num_secuencia, pr.titulo_publicacion,
                           pr.id_tipo, t.descripcion_tipo
                      FROM productos pr
                      JOIN tipo_pro t ON t.id_tipo = pr.id_tipo
                     WHERE pr.id_proyecto = ?
                     ORDER BY pr.num_secuencia
                    """, idProyecto));
            detalle.put("lineas", Sql.consultar(conexion, """
                    SELECT l.id_linea, l.nombre_linea
                      FROM proyecto_linea pl
                      JOIN linea_investigacion l ON l.id_linea = pl.id_linea
                     WHERE pl.id_proyecto = ?
                     ORDER BY l.nombre_linea
                    """, idProyecto));
            return detalle;
        });
    }

    /*
     * Creación delegada de usuarios
     * Admin crea secretaría/coordinador; secretaría crea profesor/estudiante. El SP aplica los GRANT.
     */
    public Map<String, Object> crearUsuario(SessionUser creador, CrearUsuarioRequest request) {
        String rolSolicitado = request.rol().trim().toUpperCase();
        boolean permitido = (creador.rol() == Role.ADMIN
                && rolSolicitado.matches("SECRETARIA|COORDINADOR"))
                || (creador.rol() == Role.SECRETARIA
                && rolSolicitado.matches("PROFESOR|ESTUDIANTE"));
        if (!permitido) {
            throw new ApiException(403, "El rol " + creador.rolEtiqueta()
                    + " no puede crear usuarios de tipo " + rolSolicitado + ".");
        }

        databaseService.conCatalogo(conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_crear_usuario_app",
                    creador.rol().name(), request.usuario(), request.password(), rolSolicitado,
                    request.idProfesor(), request.idEstudiante(), request.nombrePersona());
            return null;
        });
        return Map.of("mensaje", "Usuario " + request.usuario() + " creado con rol " + rolSolicitado + ".");
    }

    /*
     * Listado de usuarios
     * El administrador ve todas las cuentas; la secretaría solo ve cuentas académicas.
     */
    public List<Map<String, Object>> listarUsuarios(SessionUser usuario) {
        String filtro = usuario.rol() == Role.SECRETARIA
                ? "WHERE rol IN ('PROFESOR','ESTUDIANTE')" : "";
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion,
                "SELECT usuario, rol, id_profesor, id_estudiante, nombre_persona "
                        + "FROM usuario_persona " + filtro + " ORDER BY rol, usuario"));
    }

    public Map<String, Object> registrarProyecto(SessionUser usuario, RegistrarProyectoRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_registrar_proyecto_transaccional",
                    request.nombreProyecto(), request.acronimo(), request.presupuesto(),
                    request.programa(), request.descripcion(), request.inicioPro(),
                    request.finPro(), request.resolucion());
            return null;
        });
        return Map.of("mensaje", "Proyecto registrado correctamente mediante transaccion.");
    }

    public Map<String, Object> registrarProrroga(SessionUser usuario, Integer idProyecto, ProrrogaRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_control_prorroga_transaccional",
                    idProyecto, request.nuevaFechaFin());
            return null;
        });
        return Map.of("mensaje", "Prorroga registrada correctamente.");
    }

    public Map<String, Object> cambiarDirector(SessionUser usuario, Integer idProyecto,
            CambioDirectorRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_cambio_director_transaccional",
                    idProyecto, request.idDocenteSaliente(), request.idDocenteEntrante(),
                    request.resolucionSalida(), request.motivoSalida(), request.resolucionIngreso());
            return null;
        });
        return Map.of("mensaje", "Cambio de director ejecutado correctamente.");
    }

    public Map<String, Object> relevarDireccion(SessionUser usuario, Integer idProyecto, RelevoRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_control_responsables_transaccional",
                    idProyecto, request.idDirectorSaliente(), request.idNuevoSubrogante(),
                    request.resolucionSalida(), request.motivoSalida(), request.resolucionIngreso());
            return null;
        });
        return Map.of("mensaje", "Relevo de direccion ejecutado correctamente.");
    }

    public Map<String, Object> desvincularCierre(SessionUser usuario, Integer idProyecto) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_desvincular_cierre_transaccional", idProyecto);
            return null;
        });
        return Map.of("mensaje", "Desvinculacion por cierre ejecutada correctamente.");
    }

    public Map<String, Object> cerrarProyecto(SessionUser usuario, Integer idProyecto,
            CerrarProyectoRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.actualizar(conexion, """
                    UPDATE proyecto_investigacion
                       SET resolucion_consejo_uni_cierre = ?
                     WHERE id_proyecto = ?
                    """, request.resolucionCierre(), idProyecto);
            if (Boolean.TRUE.equals(request.desvincular())) {
                Sql.ejecutarProcedimiento(conexion, "sp_desvincular_cierre_transaccional", idProyecto);
            }
            return null;
        });
        return Map.of("mensaje", "Proyecto cerrado. El trigger de auditoria registro el cambio.");
    }

    public Map<String, Object> actualizarProyecto(SessionUser usuario, Integer idProyecto,
            RegistrarProyectoRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.actualizar(conexion, """
                    UPDATE proyecto_investigacion
                       SET nombre_proyecto = ?, acronimo = ?, presupuesto = ?, programa = ?,
                           descripcion = ?, inicio_pro = ?, fin_pro = ?, resolucion_consejo_uni_aprobado = ?
                     WHERE id_proyecto = ?
                    """, request.nombreProyecto(), request.acronimo(), request.presupuesto(),
                    request.programa(), request.descripcion(), request.inicioPro(), request.finPro(),
                    request.resolucion(), idProyecto);
            return null;
        });
        return Map.of("mensaje", "Proyecto actualizado. El trigger de auditoria registro el cambio.");
    }

    public Map<String, Object> eliminarProyecto(SessionUser usuario, Integer idProyecto) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.actualizar(conexion, "DELETE FROM proyecto_investigacion WHERE id_proyecto = ?", idProyecto);
            return null;
        });
        return Map.of("mensaje", "Proyecto eliminado. El trigger de auditoria registro la operacion.");
    }

    public Map<String, Object> asignarProfesor(SessionUser usuario, Integer idProyecto,
            AsignarProfesorRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.actualizar(conexion, """
                    INSERT INTO proyecto_profesor
                        (id_proyecto, id_profesor, cargo, resolucion_ingreso)
                    VALUES (?, ?, ?, ?)
                    """, idProyecto, request.idProfesor(), request.cargo(), request.resolucionIngreso());
            return null;
        });
        return Map.of("mensaje",
                "Profesor asignado. El trigger valido el cupo y calculo el tamano del equipo.");
    }

    public Map<String, Object> registrarEstudiante(SessionUser usuario, Integer idProyecto,
            RegistrarEstudianteRequest request) {
        databaseService.conUsuario(usuario, conexion -> {
            Sql.actualizar(conexion, """
                    INSERT INTO estudiante (id_proyecto, nombres_est, telefono_est, departamento_facultad)
                    VALUES (?, ?, ?, ?)
                    """, idProyecto, request.nombresEst(), request.telefonoEst(),
                    request.departamentoFacultad());
            return null;
        });
        return Map.of("mensaje", "Estudiante registrado correctamente.");
    }

    // ------------------------------------------------------------------
    // Productos cientificos: Factory Method + procedimientos transaccionales
    // ------------------------------------------------------------------

    public Map<String, Object> registrarProducto(SessionUser usuario, ProductoRequest request) {
        if (request.idProyecto() == null) {
            throw new ApiException(400, "Debe seleccionar el proyecto al que pertenece el producto.");
        }
        Producto producto = productoFactory.crear(request);
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_registrar_producto_transaccional",
                    producto.parametrosRegistro(request.idProyecto()));
            return null;
        });
        return Map.of("mensaje", "Producto de tipo " + producto.descripcionTipo()
                + " registrado correctamente mediante Factory Method y transaccion.");
    }

    public Map<String, Object> reclasificarProducto(SessionUser usuario, Integer idProducto,
            ProductoRequest request) {
        Producto producto = productoFactory.crear(request);
        databaseService.conUsuario(usuario, conexion -> {
            Sql.ejecutarProcedimiento(conexion, "sp_reclasificar_producto_transaccional",
                    producto.parametrosReclasificacion(idProducto));
            return null;
        });
        return Map.of("mensaje", "Producto reclasificado a " + producto.descripcionTipo()
                + " correctamente.");
    }

    public List<Map<String, Object>> listarProductos(SessionUser usuario) {
        return databaseService.conUsuario(usuario, conexion -> Sql.consultar(conexion, """
                SELECT pr.id_producto, pr.id_proyecto, p.acronimo, pr.num_secuencia,
                       pr.titulo_publicacion, pr.id_tipo, t.descripcion_tipo,
                       COALESCE(r.nombre_revista, c.nombre_congreso, l.titulo_libro, pa.nombre_patente)
                           AS medio,
                       COALESCE(r.quartil, c.ambito, l.isbn, pa.estado) AS detalle
                  FROM productos pr
                  JOIN proyecto_investigacion p ON p.id_proyecto = pr.id_proyecto
                  JOIN tipo_pro t ON t.id_tipo = pr.id_tipo
                  LEFT JOIN publicacion_revista r ON r.id_producto = pr.id_producto
                  LEFT JOIN publicacion_congreso c ON c.id_producto = pr.id_producto
                  LEFT JOIN libro l ON l.id_producto = pr.id_producto
                  LEFT JOIN patentes pa ON pa.id_producto = pr.id_producto
                 ORDER BY p.acronimo, pr.num_secuencia
                """));
    }

    // ------------------------------------------------------------------
    // Reportes y vistas (procedimientos almacenados del script SQL)
    // ------------------------------------------------------------------

    public List<Map<String, Object>> reporteCa1(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_ca1_productividad_proyectos"));
    }

    public List<Map<String, Object>> reporteCa2(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_ca2_proyectos_sobre_promedio"));
    }

    /*
     * Reportes con ámbito por rol
     * Docente y estudiante reciben datos filtrados; los roles directivos ejecutan el procedimiento completo.
     */
    public List<Map<String, Object>> reporteCa3(SessionUser usuario) {
        if (usuario.rol() == Role.PROFESOR) {
            Integer idProfesor = idProfesorSesion(usuario);
            return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                    SELECT p.acronimo, pr.num_secuencia, pr.titulo_publicacion,
                           t.descripcion_tipo AS tipo
                      FROM productos pr
                      JOIN proyecto_investigacion p ON p.id_proyecto = pr.id_proyecto
                      JOIN tipo_pro t ON t.id_tipo = pr.id_tipo
                     WHERE EXISTS (SELECT 1 FROM proyecto_profesor pp
                                    WHERE pp.id_proyecto = pr.id_proyecto
                                      AND pp.id_profesor = ?
                                      AND pp.resolucion_salida IS NULL)
                     ORDER BY p.acronimo, pr.num_secuencia DESC
                    """, idProfesor));
        }
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_ca3_ultimo_producto_proyecto"));
    }

    public List<Map<String, Object>> reporteCa4(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_ca4_valoracion_revistas"));
    }

    public List<Map<String, Object>> reporteCa5(SessionUser usuario, Integer diasAviso) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_ca5_estado_proyectos",
                        diasAviso == null ? 30 : diasAviso));
    }

    public List<Map<String, Object>> reporteCm1(SessionUser usuario, Integer idProyecto) {
        if (usuario.rol() == Role.PROFESOR) {
            Integer idProfesor = idProfesorSesion(usuario);
            return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                    SELECT p.acronimo, pp.cargo, pf.nombre, pf.telefono,
                           IFNULL(pp.resolucion_ingreso, 'Desde el inicio') AS resolucion_ingreso
                      FROM proyecto_profesor pp
                      JOIN proyecto_investigacion p ON p.id_proyecto = pp.id_proyecto
                      JOIN profesores pf ON pf.id_profesor = pp.id_profesor
                     WHERE p.resolucion_consejo_uni_cierre IS NULL
                       AND pp.resolucion_salida IS NULL
                       AND EXISTS (SELECT 1 FROM proyecto_profesor mia
                                    WHERE mia.id_proyecto = pp.id_proyecto
                                      AND mia.id_profesor = ?
                                      AND mia.resolucion_salida IS NULL)
                     ORDER BY p.acronimo, pp.cargo, pf.nombre
                    """, idProfesor));
        }
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_cm1_equipo_proyecto", idProyecto));
    }

    public List<Map<String, Object>> reporteCm2(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_cm2_carga_profesores"));
    }

    public List<Map<String, Object>> reporteCm3(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_cm3_estudiantes_por_proyecto"));
    }

    public List<Map<String, Object>> reporteCm4(SessionUser usuario) {
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_cm4_cobertura_lineas"));
    }

    public List<Map<String, Object>> reporteCm5(SessionUser usuario) {
        if (usuario.rol() == Role.ESTUDIANTE) {
            Integer idEstudiante = idEstudianteSesion(usuario);
            return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                    SELECT p.acronimo, pr.num_secuencia AS sec, pr.titulo_publicacion AS titulo,
                           t.descripcion_tipo AS tipo,
                           COALESCE(r.nombre_revista, c.nombre_congreso, l.titulo_libro, pa.nombre_patente) AS medio,
                           COALESCE(r.quartil, c.ambito, l.isbn, pa.estado) AS detalle
                      FROM productos pr
                      JOIN proyecto_investigacion p ON p.id_proyecto = pr.id_proyecto
                      JOIN tipo_pro t ON t.id_tipo = pr.id_tipo
                      LEFT JOIN publicacion_revista r ON r.id_producto = pr.id_producto
                      LEFT JOIN publicacion_congreso c ON c.id_producto = pr.id_producto
                      LEFT JOIN libro l ON l.id_producto = pr.id_producto
                      LEFT JOIN patentes pa ON pa.id_producto = pr.id_producto
                     WHERE EXISTS (SELECT 1 FROM estudiante e
                                    WHERE e.id_proyecto = pr.id_proyecto
                                      AND e.id_estudiante = ?)
                     ORDER BY p.acronimo, pr.num_secuencia
                    """, idEstudiante));
        }
        return databaseService.conUsuario(usuario,
                conexion -> Sql.llamar(conexion, "sp_cm5_catalogo_productos"));
    }

    /*
     * Vistas personales
     * La vista del docente se filtra por su id de persona en lugar de depender del nombre del usuario MariaDB.
     */
    public List<Map<String, Object>> vistaDocente(SessionUser usuario) {
        if (usuario.rol() == Role.PROFESOR) {
            Integer idProfesor = idProfesorSesion(usuario);
            return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                    SELECT p.acronimo, p.nombre_proyecto, pp.cargo, p.presupuesto,
                           p.inicio_pro, p.fin_pro, p.prorroga, p.resolucion_consejo_uni_cierre
                      FROM proyecto_profesor pp
                      JOIN proyecto_investigacion p ON p.id_proyecto = pp.id_proyecto
                     WHERE pp.id_profesor = ?
                       AND pp.resolucion_salida IS NULL
                       AND p.resolucion_consejo_uni_cierre IS NULL
                     ORDER BY p.acronimo
                    """, idProfesor));
        }
        return databaseService.conUsuario(usuario,
                conexion -> Sql.consultar(conexion, "SELECT * FROM vw_docente_mis_proyectos"));
    }

    public List<Map<String, Object>> vistaEstudiante(SessionUser usuario) {
        if (usuario.rol() == Role.ESTUDIANTE) {
            Integer idEstudiante = idEstudianteSesion(usuario);
            return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                    SELECT p.acronimo, p.nombre_proyecto, e.nombres_est,
                           e.departamento_facultad, p.inicio_pro, p.fin_pro, p.prorroga
                      FROM estudiante e
                      JOIN proyecto_investigacion p ON p.id_proyecto = e.id_proyecto
                     WHERE e.id_estudiante = ?
                     ORDER BY p.acronimo
                    """, idEstudiante));
        }
        return databaseService.conUsuario(usuario,
                conexion -> Sql.consultar(conexion, "SELECT * FROM vw_estudiante_mis_proyectos"));
    }

    public List<Map<String, Object>> proyectosPorDocente(SessionUser usuario, Integer idProfesor) {
        Integer id = usuario.rol() == Role.PROFESOR ? idProfesorSesion(usuario) : idProfesor;
        if (id == null) {
            throw new ApiException(400, "Debe indicar el docente a consultar.");
        }
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT p.acronimo, p.nombre_proyecto, pp.cargo, p.presupuesto,
                       p.inicio_pro, p.fin_pro, p.prorroga, p.resolucion_consejo_uni_cierre
                  FROM proyecto_profesor pp
                  JOIN proyecto_investigacion p ON p.id_proyecto = pp.id_proyecto
                 WHERE pp.id_profesor = ?
                 ORDER BY p.acronimo
                """, id));
    }

    public List<Map<String, Object>> proyectosPorEstudiante(SessionUser usuario, Integer idEstudiante) {
        Integer id = usuario.rol() == Role.ESTUDIANTE ? idEstudianteSesion(usuario) : idEstudiante;
        if (id == null) {
            throw new ApiException(400, "Debe indicar el estudiante a consultar.");
        }
        return databaseService.conCatalogo(conexion -> Sql.consultar(conexion, """
                SELECT p.acronimo, p.nombre_proyecto, e.nombres_est,
                       e.departamento_facultad, p.inicio_pro, p.fin_pro, p.prorroga
                  FROM estudiante e
                  JOIN proyecto_investigacion p ON p.id_proyecto = e.id_proyecto
                 WHERE e.id_estudiante = ?
                """, id));
    }

    // ------------------------------------------------------------------
    // Auditoria (triggers de la base de datos)
    // ------------------------------------------------------------------

    public List<Map<String, Object>> auditoriaOperaciones(SessionUser usuario) {
        return databaseService.conUsuario(usuario, conexion -> Sql.consultar(conexion, """
                SELECT id_auditoria, fecha, operacion, procedimiento_ejecutado, resultado
                  FROM auditoria_operaciones
                 ORDER BY fecha DESC, id_auditoria DESC
                """));
    }

    public List<Map<String, Object>> auditoriaProyectos(SessionUser usuario) {
        return databaseService.conUsuario(usuario, conexion -> Sql.consultar(conexion, """
                SELECT id_auditoria, id_proyecto, operacion, datos_anteriores, datos_nuevos,
                       usuario, fecha_hora
                  FROM auditoria_proyecto
                 ORDER BY fecha_hora DESC, id_auditoria DESC
                """));
    }

    public List<Map<String, Object>> auditoriaParticipacion(SessionUser usuario) {
        return databaseService.conUsuario(usuario, conexion -> Sql.consultar(conexion, """
                SELECT id_historial, id_proyecto, id_profesor, cargo_anterior, cargo_nuevo,
                       resolucion_salida, motivo_salida, usuario, fecha_cambio
                  FROM historial_participacion
                 ORDER BY fecha_cambio DESC, id_historial DESC
                """));
    }
}
