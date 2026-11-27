/*
 * BLOQUE: Estado global
 * Guarda la sesión, el rol, la persona vinculada y la caché de catálogos.
 */
const state = {
    usuario: null,
    rol: null,
    rolEtiqueta: null,
    nombrePersona: null,
    catalogos: {
        cargados: false,
        proyectos: [],
        profesores: [],
        estudiantes: [],
        lineas: [],
        tipos: []
    }
};

/*
 * BLOQUE: Configuración de navegación
 * Cada opción declara los roles autorizados que pueden verla.
 */
const MENU = [
    { id: 'inicio', label: 'Inicio', icon: 'I', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR', 'PROFESOR', 'ESTUDIANTE'] },
    { id: 'proyectos', label: 'Proyectos', icon: 'P', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'] },
    { id: 'operaciones', label: 'Operaciones', icon: 'O', roles: ['ADMIN', 'SECRETARIA'] },
    { id: 'usuarios', label: 'Usuarios', icon: 'U', roles: ['ADMIN', 'SECRETARIA'] },
    { id: 'productos', label: 'Productos', icon: 'C', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'] },
    { id: 'reportes', label: 'Reportes', icon: 'R', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'] },
    { id: 'profesor', label: 'Mi espacio', icon: 'D', roles: ['PROFESOR'] },
    { id: 'estudiante', label: 'Mi espacio', icon: 'E', roles: ['ESTUDIANTE'] },
    { id: 'auditoria', label: 'Auditoria', icon: 'A', roles: ['ADMIN'] }
];

const PAGINAS = {
    inicio: { titulo: 'Inicio', eyebrow: 'Panel' },
    proyectos: { titulo: 'Proyectos de investigacion', eyebrow: 'Gestion' },
    operaciones: { titulo: 'Operaciones transaccionales', eyebrow: 'Procedimientos almacenados' },
    usuarios: { titulo: 'Gestion de usuarios', eyebrow: 'Seguridad' },
    productos: { titulo: 'Productos cientificos', eyebrow: 'Factory Method' },
    reportes: { titulo: 'Reportes y consultas', eyebrow: 'Procedimientos almacenados' },
    profesor: { titulo: 'Mi espacio docente', eyebrow: 'Datos propios' },
    estudiante: { titulo: 'Mi espacio estudiante', eyebrow: 'Datos propios' },
    auditoria: { titulo: 'Auditoria', eyebrow: 'Triggers de base de datos' }
};

/*
 * BLOQUE: Catálogo de reportes
 * Define endpoint, descripción y roles permitidos para cada procedimiento.
 */
const REPORTES = [
    { id: 'ca1', titulo: 'CA1 - Productividad por proyecto', descripcion: 'Cantidad de productos por proyecto y tipo, con subtotales y total UEB.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/ca1' },
    { id: 'ca2', titulo: 'CA2 - Proyectos sobre el promedio', descripcion: 'Proyectos con presupuesto superior al promedio institucional.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/ca2' },
    { id: 'ca3', titulo: 'CA3 - Ultimo producto por proyecto', descripcion: 'Ultima publicacion registrada por cada proyecto de investigacion.', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/ca3' },
    { id: 'ca4', titulo: 'CA4 - Valoracion de revistas', descripcion: 'Puntaje de revistas segun quartil y cumplimiento del minimo institucional.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/ca4' },
    { id: 'ca5', titulo: 'CA5 - Estado de proyectos', descripcion: 'Estado, meses de ejecucion y dias restantes de cada proyecto.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/ca5' },
    { id: 'cm1', titulo: 'CM1 - Equipo por proyecto', descripcion: 'Docentes activos y cargos de un proyecto o de todos los proyectos.', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/cm1' },
    { id: 'cm2', titulo: 'CM2 - Carga de profesores', descripcion: 'Proyectos activos por docente y disponibilidad para asumir uno nuevo.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/cm2' },
    { id: 'cm3', titulo: 'CM3 - Estudiantes por proyecto', descripcion: 'Cantidad y nombres de estudiantes vinculados a cada proyecto.', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/cm3' },
    { id: 'cm4', titulo: 'CM4 - Cobertura de lineas', descripcion: 'Profesores, proyectos y presupuesto asignado por linea de investigacion.', roles: ['ADMIN', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/cm4' },
    { id: 'cm5', titulo: 'CM5 - Catalogo de productos', descripcion: 'Catalogo general de productos cientificos con su medio de publicacion.', roles: ['ADMIN', 'SECRETARIA', 'COORDINADOR', 'DIRECTOR'], url: '/api/reportes/cm5' },
    { id: 'vista-docente', titulo: 'Vista vw_docente_mis_proyectos', descripcion: 'Vista restringida por CURRENT_USER() para el rol docente.', roles: ['ADMIN'], url: '/api/reportes/vista-docente' },
    { id: 'vista-estudiante', titulo: 'Vista vw_estudiante_mis_proyectos', descripcion: 'Vista restringida por CURRENT_USER() para el rol estudiante.', roles: ['ADMIN'], url: '/api/reportes/vista-estudiante' }
];

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

/*
 * BLOQUE: Arranque
 * Registra eventos globales y recupera la sesión activa al cargar la página.
 */
document.addEventListener('DOMContentLoaded', init);

async function init() {
    $('#login-form').addEventListener('submit', login);
    $('#logout-btn').addEventListener('click', logout);

    document.addEventListener('click', (event) => {
        const botonNav = event.target.closest('[data-nav]');
        if (botonNav) {
            irA(botonNav.dataset.nav);
        }
    });

    await cargarSesion();
}

/*
 * BLOQUE: Autenticación
 * Login, logout, recuperación de sesión y transición entre login y panel.
 */
async function cargarSesion() {
    try {
        const sesion = await api('/api/auth/me');
        state.usuario = sesion.usuario;
        state.rol = sesion.rol;
        state.rolEtiqueta = sesion.rolEtiqueta;
        state.nombrePersona = sesion.nombrePersona;
        iniciarApp();
    } catch (error) {
        mostrarLogin();
    }
}

async function login(event) {
    event.preventDefault();
    const cajaError = $('#login-error');
    cajaError.classList.add('hidden');

    const usuario = $('#login-usuario').value.trim();
    const password = $('#login-password').value;

    try {
        const sesion = await api('/api/auth/login', {
            method: 'POST',
            body: { usuario, password }
        });
        state.usuario = sesion.usuario;
        state.rol = sesion.rol;
        state.rolEtiqueta = sesion.rolEtiqueta;
        state.nombrePersona = sesion.nombrePersona;
        iniciarApp();
        toast('Bienvenido' + (sesion.nombrePersona ? ', ' + sesion.nombrePersona : '') + '.', 'success');
    } catch (error) {
        cajaError.textContent = error.message;
        cajaError.classList.remove('hidden');
    }
}

async function logout() {
    try {
        await api('/api/auth/logout', { method: 'POST' });
    } catch (error) {
        // La sesion pudo haber expirado; se continua cerrando en el cliente.
    }
    mostrarLogin();
    toast('Sesion finalizada.', 'success');
}

function mostrarLogin() {
    state.usuario = null;
    state.rol = null;
    state.rolEtiqueta = null;
    state.nombrePersona = null;
    state.catalogos.cargados = false;
    $('#app-view').classList.add('hidden');
    $('#login-view').classList.remove('hidden');
    $('#login-password').value = '';
}

function iniciarApp() {
    $('#login-view').classList.add('hidden');
    $('#app-view').classList.remove('hidden');
    $('#user-box').innerHTML = '<strong>' + esc(state.nombrePersona || state.usuario) + '</strong>' +
        '<span>' + esc(state.rolEtiqueta) + ' · ' + esc(state.usuario) + '</span>';
    $('#topbar-user').textContent = state.rolEtiqueta + (state.nombrePersona ? ' · ' + state.nombrePersona : '');
    construirNav();
    irA('inicio');
}

function construirNav() {
    const items = MENU.filter((item) => item.roles.includes(state.rol));
    $('#nav').innerHTML = items.map((item) => (
        '<button type="button" data-nav="' + item.id + '">' +
        '<span class="nav-icon">' + esc(item.icon) + '</span>' + esc(item.label) +
        '</button>'
    )).join('');
}

async function irA(id) {
    const pagina = PAGINAS[id] || { titulo: id, eyebrow: 'Panel' };
    $('#page-title').textContent = pagina.titulo;
    $('#page-eyebrow').textContent = pagina.eyebrow;

    $$('#nav button').forEach((boton) => {
        boton.classList.toggle('active', boton.dataset.nav === id);
    });

    setContenido(cargando());
    try {
        await RENDERERS[id]();
    } catch (error) {
        setContenido('<div class="card">' + errorBox(error.message) + '</div>');
        toast(error.message, 'error');
    }
}

const RENDERERS = {
    inicio: renderInicio,
    proyectos: renderProyectos,
    operaciones: renderOperaciones,
    usuarios: renderUsuarios,
    productos: renderProductos,
    reportes: renderReportes,
    profesor: renderProfesor,
    estudiante: renderEstudiante,
    auditoria: renderAuditoria
};

/*
 * BLOQUE: Navegación
 * Construye el encabezado y delega el contenido a la página solicitada.
 */
async function renderInicio() {
    const esAdministrativo = ['ADMIN', 'COORDINADOR', 'DIRECTOR'].includes(state.rol);
    let html = '<section class="hero-card">' +
        '<p class="eyebrow">Sistema web</p>' +
        '<h2>Hola, ' + esc(state.usuario) + '</h2>' +
        '<p>Rol activo: <strong>' + esc(state.rolEtiqueta) + '</strong>. ' +
        'La aplicacion combina una API REST en Spring Boot, una base MariaDB y los procedimientos almacenados del proyecto de investigacion.</p>' +
        '<div class="hero-actions">' +
        '<button type="button" class="btn" data-nav="reportes">Ver reportes</button>' +
        (state.rol === 'ADMIN' || state.rol === 'SECRETARIA'
            ? '<button type="button" class="btn" data-nav="operaciones">Operaciones transaccionales</button>'
            : '') +
        '</div></section>';

    if (esAdministrativo) {
        const resumen = await api('/api/dashboard');
        html += '<div class="grid-4">' +
            tarjetaResumen('Proyectos', resumen.proyectos) +
            tarjetaResumen('Proyectos activos', resumen.proyectosActivos) +
            tarjetaResumen('Docentes', resumen.profesores) +
            tarjetaResumen('Estudiantes', resumen.estudiantes) +
            '</div>' +
            '<div class="grid-3">' +
            tarjetaResumen('Productos cientificos', resumen.productos) +
            tarjetaResumen('Lineas de investigacion', resumen.lineas) +
            '<div class="card"><h3>Accesos rapidos</h3>' +
            '<div class="form-actions"><button type="button" class="btn btn-primary" data-nav="proyectos">Proyectos</button>' +
            '<button type="button" class="btn" data-nav="productos">Productos</button></div></div>' +
            '</div>';
    } else if (state.rol === 'SECRETARIA') {
        html += '<div class="card"><h3>Funciones habilitadas para secretaria</h3>' +
            '<p class="muted">Este rol atiende a docentes y estudiantes: gestiona proyectos, prorrogas, equipo, estudiantes, cambios de direccion, cierre y creacion de cuentas academicas.</p>' +
            '<div class="form-actions"><button type="button" class="btn btn-primary" data-nav="operaciones">Ir a operaciones</button>' +
            '<button type="button" class="btn" data-nav="proyectos">Ver proyectos</button></div></div>';
    } else if (state.rol === 'PROFESOR') {
        const [proyectos, productos] = await Promise.all([
            api('/api/reportes/vista-docente'),
            api('/api/reportes/ca3')
        ]);
        html += '<div class="grid-3">' +
            tarjetaResumen('Mis proyectos activos', proyectos.length) +
            tarjetaResumen('Mis productos', productos.length) +
            '<div class="card"><h3>Mi información</h3><p class="muted">' +
            esc(state.nombrePersona || state.usuario) + '</p>' +
            '<button type="button" class="btn btn-primary" data-nav="profesor">Abrir mi espacio</button></div>' +
            '</div>';
    } else if (state.rol === 'ESTUDIANTE') {
        const [proyectos, productos] = await Promise.all([
            api('/api/reportes/vista-estudiante'),
            api('/api/reportes/cm5')
        ]);
        html += '<div class="grid-3">' +
            tarjetaResumen('Mis proyectos', proyectos.length) +
            tarjetaResumen('Productos de mis proyectos', productos.length) +
            '<div class="card"><h3>Mi información</h3><p class="muted">' +
            esc(state.nombrePersona || state.usuario) + '</p>' +
            '<button type="button" class="btn btn-primary" data-nav="estudiante">Abrir mi espacio</button></div>' +
            '</div>';
    }

    setContenido(html);
}

function tarjetaResumen(etiqueta, valor) {
    return '<div class="stat-card"><div class="stat-label">' + esc(etiqueta) + '</div>' +
        '<div class="stat-value">' + esc(formatear(valor)) + '</div></div>';
}

/*
 * BLOQUE: Proyectos
 * Listado general y detalle para roles con permiso de lectura.
 */
async function renderProyectos() {
    const proyectos = await api('/api/proyectos');
    const filas = proyectos.map((proyecto) => (
        '<tr>' +
        '<td>' + esc(proyecto.acronimo) + '</td>' +
        '<td>' + esc(proyecto.nombre_proyecto) + '</td>' +
        '<td>' + esc(formatear(proyecto.presupuesto)) + '</td>' +
        '<td>' + esc(formatear(proyecto.inicio_pro)) + '</td>' +
        '<td>' + esc(formatear(proyecto.fin_pro)) + '</td>' +
        '<td>' + esc(formatear(proyecto.prorroga)) + '</td>' +
        '<td>' + badgeEstado(proyecto.estado) + '</td>' +
        '<td>' + esc(proyecto.total_productos) + '</td>' +
        '<td>' + esc(proyecto.docentes_activos) + '</td>' +
        '<td>' + esc(proyecto.estudiantes) + '</td>' +
        '<td><button type="button" class="btn btn-sm btn-primary" data-detalle="' + proyecto.id_proyecto + '">Ver detalle</button></td>' +
        '</tr>'
    )).join('');

    setContenido('<div class="card">' +
        '<div class="card-head"><div><h2>Proyectos registrados</h2>' +
        '<p class="card-sub">Consulta directa a las tablas con la conexion del usuario autenticado.</p></div>' +
        '<button type="button" class="btn" id="btn-actualizar-proyectos">Actualizar</button></div>' +
        '<div class="table-wrap"><table class="tabla"><thead><tr>' +
        '<th>Acronimo</th><th>Nombre</th><th>Presupuesto</th><th>Inicio</th><th>Fin</th>' +
        '<th>Prorroga</th><th>Estado</th><th>Productos</th><th>Docentes</th><th>Estudiantes</th><th></th>' +
        '</tr></thead><tbody>' + (filas || '<tr><td colspan="11">Sin proyectos registrados.</td></tr>') +
        '</tbody></table></div></div>' +
        '<div id="detalle-proyecto"></div>');

    $('#btn-actualizar-proyectos').addEventListener('click', () => renderProyectos());
    $$('[data-detalle]').forEach((boton) => {
        boton.addEventListener('click', () => verDetalleProyecto(boton.dataset.detalle));
    });
}

async function verDetalleProyecto(idProyecto) {
    const contenedor = $('#detalle-proyecto');
    contenedor.innerHTML = cargando('Cargando detalle del proyecto...');
    try {
        const detalle = await api('/api/proyectos/' + idProyecto);
        const proyecto = detalle.proyecto;
        if (!proyecto) {
            contenedor.innerHTML = '<div class="card">' + errorBox('El proyecto no existe.') + '</div>';
            return;
        }

        const equipo = tabla(detalle.equipo, ['nombre', 'cargo', 'resolucion_ingreso', 'resolucion_salida', 'motivo_salida']);
        const estudiantes = tabla(detalle.estudiantes, ['nombres_est', 'telefono_est', 'departamento_facultad']);
        const productos = tabla(detalle.productos, ['num_secuencia', 'titulo_publicacion', 'descripcion_tipo']);
        const lineas = tabla(detalle.lineas, ['nombre_linea']);

        contenedor.innerHTML = '<div class="card" style="margin-top:18px">' +
            '<div class="card-head"><div><h2>' + esc(proyecto.acronimo) + ' - ' + esc(proyecto.nombre_proyecto) + '</h2>' +
            '<p class="card-sub">' + esc(proyecto.programa) + '</p></div>' + badgeEstado(estadoProyecto(proyecto)) + '</div>' +
            '<dl class="kv">' +
            '<dt>Presupuesto</dt><dd>' + esc(formatear(proyecto.presupuesto)) + '</dd>' +
            '<dt>Inicio</dt><dd>' + esc(formatear(proyecto.inicio_pro)) + '</dd>' +
            '<dt>Fin original</dt><dd>' + esc(formatear(proyecto.fin_pro)) + '</dd>' +
            '<dt>Prorroga</dt><dd>' + esc(formatear(proyecto.prorroga)) + '</dd>' +
            '<dt>Resolucion aprobacion</dt><dd>' + esc(proyecto.resolucion_consejo_uni_aprobado) + '</dd>' +
            '<dt>Resolucion cierre</dt><dd>' + esc(formatear(proyecto.resolucion_consejo_uni_cierre)) + '</dd>' +
            '<dt>Productos</dt><dd>' + esc(proyecto.total_productos) + '</dd>' +
            '<dt>Descripcion</dt><dd>' + esc(proyecto.descripcion) + '</dd>' +
            '</dl>' +
            '<div class="detail-grid">' +
            '<div class="detail-box"><h4>Equipo docente</h4>' + equipo + '</div>' +
            '<div class="detail-box"><h4>Estudiantes</h4>' + estudiantes + '</div>' +
            '<div class="detail-box"><h4>Productos cientificos</h4>' + productos + '</div>' +
            '<div class="detail-box"><h4>Lineas de investigacion</h4>' + lineas + '</div>' +
            '</div></div>';

        contenedor.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        contenedor.innerHTML = '<div class="card">' + errorBox(error.message) + '</div>';
    }
}

/*
 * BLOQUE: Operaciones transaccionales
 * Formularios que invocan los procedimientos de registro, prórroga, director, relevo y cierre.
 */
async function renderOperaciones() {
    await cargarCatalogos();

    let html = '<section class="hero-card"><p class="eyebrow">Base de datos</p>' +
        '<h2>Operaciones transaccionales</h2>' +
        '<p>Cada formulario ejecuta un procedimiento almacenado o una sentencia controlada por la base de datos. ' +
        'Los errores de negocio se muestran con el mensaje devuelto por MariaDB (SQLSTATE 45000).</p></section>';

    html += '<div class="grid-2">';
    html += tarjetaOperacion('Registrar proyecto', formularioRegistrarProyecto(), true);
    html += tarjetaOperacion('Registrar prorroga', formularioProrroga());
    html += tarjetaOperacion('Cambio de director', formularioCambioDirector());
    html += tarjetaOperacion('Relevo de direccion', formularioRelevo());
    html += tarjetaOperacion('Desvincular por cierre', formularioCierre());
    html += tarjetaOperacion('Asignar profesor al equipo', formularioAsignarProfesor());
    html += tarjetaOperacion('Registrar estudiante', formularioRegistrarEstudiante());
    html += tarjetaOperacion('Registrar linea de investigacion', formularioRegistrarLinea());
    html += tarjetaOperacion('Asignar linea a proyecto', formularioAsignarLineaProyecto());
    html += tarjetaOperacion('Asignar linea a docente', formularioAsignarLineaDocente());
    html += tarjetaOperacion('Actualizar proyecto', formularioActualizarProyecto());
    html += tarjetaOperacion('Eliminar proyecto', formularioEliminarProyecto());
    html += '</div>';

    setContenido(html);
    bindOperaciones();
}

function tarjetaOperacion(titulo, contenido, abierta = false) {
    return '<details class="operation-card"' + (abierta ? ' open' : '') + '>' +
        '<summary>' + esc(titulo) + '</summary>' +
        '<div class="operation-body">' + contenido + '</div></details>';
}

function formularioRegistrarProyecto() {
    return '<form id="op-proyecto"><div class="form-grid">' +
        '<div class="full"><label>Nombre del proyecto</label><input id="np-nombre" required></div>' +
        '<div><label>Acronimo</label><input id="np-acronimo" required maxlength="30"></div>' +
        '<div><label>Presupuesto</label><input id="np-presupuesto" type="number" step="0.01" min="0" required></div>' +
        '<div class="full"><label>Programa</label><input id="np-programa" required></div>' +
        '<div><label>Fecha de inicio</label><input id="np-inicio" type="date" required></div>' +
        '<div><label>Fecha de fin</label><input id="np-fin" type="date" required></div>' +
        '<div class="full"><label>Resolucion de aprobacion</label><input id="np-resolucion" required></div>' +
        '<div class="full"><label>Descripcion</label><textarea id="np-descripcion"></textarea></div>' +
        '</div><div class="form-actions"><button type="submit" class="btn btn-primary">Registrar proyecto</button></div></form>';
}

function formularioProrroga() {
    return '<form id="op-prorroga"><label>Proyecto</label>' + selectProyectos('pr-proyecto') +
        '<label style="margin-top:10px">Nueva fecha de fin</label><input id="pr-fecha" type="date" required>' +
        '<p class="hint">La base de datos valida que la prorroga no supere los 3 meses y que el proyecto no este cerrado.</p>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Registrar prorroga</button></div></form>';
}

function formularioCambioDirector() {
    return '<form id="op-cambio"><label>Proyecto</label>' + selectProyectos('cd-proyecto') +
        '<label style="margin-top:10px">Director saliente activo</label><select id="cd-saliente"></select>' +
        '<label style="margin-top:10px">Nuevo director</label>' + selectProfesores('cd-entrante') +
        '<div class="form-grid" style="margin-top:10px">' +
        '<div><label>Resolucion de salida</label><input id="cd-res-salida" required></div>' +
        '<div><label>Resolucion de ingreso</label><input id="cd-res-ingreso" required></div>' +
        '<div class="full"><label>Motivo de salida</label><input id="cd-motivo" required></div>' +
        '</div><div id="cd-equipo" style="margin-top:12px"></div>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Ejecutar cambio</button></div></form>';
}

function formularioRelevo() {
    return '<form id="op-relevo"><label>Proyecto</label>' + selectProyectos('rv-proyecto') +
        '<label style="margin-top:10px">Director saliente activo</label><select id="rv-saliente"></select>' +
        '<label style="margin-top:10px">Nuevo director subrogante</label>' + selectProfesores('rv-subrogante') +
        '<div class="form-grid" style="margin-top:10px">' +
        '<div><label>Resolucion de salida</label><input id="rv-res-salida" required></div>' +
        '<div><label>Resolucion de ingreso</label><input id="rv-res-ingreso" required></div>' +
        '<div class="full"><label>Motivo de salida</label><input id="rv-motivo" required></div>' +
        '</div><div id="rv-equipo" style="margin-top:12px"></div>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Ejecutar relevo</button></div></form>';
}

function formularioCierre() {
    return '<form id="op-cierre"><label>Proyecto</label>' + selectProyectos('ci-proyecto') +
        '<label style="margin-top:10px">Resolucion de cierre</label><input id="ci-resolucion" required>' +
        '<label style="margin-top:10px; display:flex; gap:8px; align-items:center; font-weight:600">' +
        '<input id="ci-desvincular" type="checkbox" checked style="width:auto"> Desvincular personal y estudiantes</label>' +
        '<p class="hint">El cierre actualiza la resolucion y dispara la auditoria. La desvinculacion ejecuta sp_desvincular_cierre_transaccional.</p>' +
        '<div class="form-actions"><button type="submit" class="btn btn-danger">Cerrar proyecto</button></div></form>';
}

function formularioAsignarProfesor() {
    return '<form id="op-profesor"><label>Proyecto</label>' + selectProyectos('ap-proyecto') +
        '<label style="margin-top:10px">Profesor</label>' + selectProfesores('ap-profesor') +
        '<div class="form-grid" style="margin-top:10px">' +
        '<div><label>Cargo</label><select id="ap-cargo">' +
        '<option value="Investigador">Investigador</option>' +
        '<option value="Director subrogante">Director subrogante</option>' +
        '<option value="Director">Director</option>' +
        '</select></div>' +
        '<div><label>Resolucion de ingreso</label><input id="ap-resolucion"></div>' +
        '</div><p class="hint">El trigger trg_validar_participacion controla el cupo maximo y calcula num_profesores.</p>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Asignar profesor</button></div></form>';
}

function formularioRegistrarEstudiante() {
    return '<form id="op-estudiante"><label>Proyecto</label>' + selectProyectos('ae-proyecto') +
        '<label style="margin-top:10px">Nombres del estudiante</label><input id="ae-nombre" required>' +
        '<label style="margin-top:10px">Telefono</label><input id="ae-telefono">' +
        '<label style="margin-top:10px">Departamento o facultad</label><input id="ae-departamento" required>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Registrar estudiante</button></div></form>';
}

function formularioRegistrarLinea() {
    return '<form id="op-linea"><div class="form-grid">' +
        '<div class="full"><label>Nombre de la linea</label><input id="ln-nombre" required></div>' +
        '<div class="full"><label>Descriptores</label><input id="ln-descriptores"></div>' +
        '</div><div class="form-actions"><button type="submit" class="btn btn-primary">Registrar linea</button></div></form>';
}

function formularioAsignarLineaProyecto() {
    return '<form id="op-linea-proyecto"><label>Proyecto</label>' + selectProyectos('lpr-proyecto') +
        '<label style="margin-top:10px">Linea de investigacion</label>' + selectLineas('lpr-linea') +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Asignar al proyecto</button></div></form>';
}

function formularioAsignarLineaDocente() {
    return '<form id="op-linea-docente"><label>Docente</label>' + selectProfesores('ldo-docente') +
        '<label style="margin-top:10px">Linea de investigacion</label>' + selectLineas('ldo-linea') +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Asignar al docente</button></div></form>';
}

function formularioActualizarProyecto() {
    return '<form id="op-actualizar"><label>Proyecto</label>' + selectProyectos('ac-proyecto') +
        '<div class="form-grid" style="margin-top:10px">' +
        '<div class="full"><label>Nombre</label><input id="ac-nombre" required></div>' +
        '<div><label>Acronimo</label><input id="ac-acronimo" required></div>' +
        '<div><label>Presupuesto</label><input id="ac-presupuesto" type="number" step="0.01" min="0" required></div>' +
        '<div class="full"><label>Programa</label><input id="ac-programa" required></div>' +
        '<div><label>Inicio</label><input id="ac-inicio" type="date" required></div>' +
        '<div><label>Fin</label><input id="ac-fin" type="date" required></div>' +
        '<div class="full"><label>Resolucion de aprobacion</label><input id="ac-resolucion" required></div>' +
        '<div class="full"><label>Descripcion</label><textarea id="ac-descripcion"></textarea></div>' +
        '</div><p class="hint">El trigger trg_auditoria_proyecto_upd registra los valores anteriores y nuevos.</p>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Actualizar proyecto</button></div></form>';
}

function formularioEliminarProyecto() {
    return '<form id="op-eliminar"><label>Proyecto</label>' + selectProyectos('el-proyecto') +
        '<p class="hint">El borrado en cascada elimina equipo, estudiantes y productos. El trigger de auditoria conserva la trazabilidad.</p>' +
        '<div class="form-actions"><button type="submit" class="btn btn-danger">Eliminar proyecto</button></div></form>';
}

function bindOperaciones() {
    enFormulario('op-proyecto', async () => {
        const respuesta = await api('/api/proyectos', {
            method: 'POST',
            body: {
                nombreProyecto: valor('np-nombre'),
                acronimo: valor('np-acronimo'),
                presupuesto: valor('np-presupuesto'),
                programa: valor('np-programa'),
                descripcion: valor('np-descripcion') || null,
                inicioPro: valor('np-inicio'),
                finPro: valor('np-fin'),
                resolucion: valor('np-resolucion')
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-prorroga', async () => {
        const id = valor('pr-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/prorroga', {
            method: 'POST',
            body: { nuevaFechaFin: valor('pr-fecha') }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-cambio', async () => {
        const id = valor('cd-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/cambio-director', {
            method: 'POST',
            body: {
                idDocenteSaliente: Number(valor('cd-saliente')),
                idDocenteEntrante: Number(valor('cd-entrante')),
                resolucionSalida: valor('cd-res-salida'),
                motivoSalida: valor('cd-motivo'),
                resolucionIngreso: valor('cd-res-ingreso')
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
        actualizarEquipoCambio();
    });

    enFormulario('op-relevo', async () => {
        const id = valor('rv-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/relevo', {
            method: 'POST',
            body: {
                idDirectorSaliente: Number(valor('rv-saliente')),
                idNuevoSubrogante: Number(valor('rv-subrogante')),
                resolucionSalida: valor('rv-res-salida'),
                motivoSalida: valor('rv-motivo'),
                resolucionIngreso: valor('rv-res-ingreso')
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
        actualizarEquipoRelevo();
    });

    enFormulario('op-cierre', async () => {
        const id = valor('ci-proyecto');
        const desvincular = $('#ci-desvincular').checked;
        if (!confirm('Se registrara el cierre del proyecto' + (desvincular ? ' y se desvinculara al personal y estudiantes' : '') + '. ¿Continuar?')) {
            return;
        }
        const respuesta = await api('/api/proyectos/' + id + '/cerrar', {
            method: 'POST',
            body: {
                resolucionCierre: valor('ci-resolucion'),
                desvincular: desvincular
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-profesor', async () => {
        const id = valor('ap-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/equipo', {
            method: 'POST',
            body: {
                idProfesor: Number(valor('ap-profesor')),
                cargo: valor('ap-cargo'),
                resolucionIngreso: valor('ap-resolucion') || null
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-estudiante', async () => {
        const id = valor('ae-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/estudiantes', {
            method: 'POST',
            body: {
                nombresEst: valor('ae-nombre'),
                telefonoEst: valor('ae-telefono') || null,
                departamentoFacultad: valor('ae-departamento')
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-linea', async () => {
        const respuesta = await api('/api/lineas', {
            method: 'POST',
            body: {
                nombreLinea: valor('ln-nombre'),
                descriptores: valor('ln-descriptores') || null
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-linea-proyecto', async () => {
        const id = valor('lpr-proyecto');
        const respuesta = await api('/api/proyectos/' + id + '/lineas', {
            method: 'POST',
            body: { idLinea: Number(valor('lpr-linea')) }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-linea-docente', async () => {
        const id = valor('ldo-docente');
        const respuesta = await api('/api/docentes/' + id + '/lineas', {
            method: 'POST',
            body: { idLinea: Number(valor('ldo-linea')) }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-actualizar', async () => {
        const id = valor('ac-proyecto');
        const respuesta = await api('/api/proyectos/' + id, {
            method: 'PUT',
            body: {
                nombreProyecto: valor('ac-nombre'),
                acronimo: valor('ac-acronimo'),
                presupuesto: valor('ac-presupuesto'),
                programa: valor('ac-programa'),
                descripcion: valor('ac-descripcion') || null,
                inicioPro: valor('ac-inicio'),
                finPro: valor('ac-fin'),
                resolucion: valor('ac-resolucion')
            }
        });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    enFormulario('op-eliminar', async () => {
        const id = valor('el-proyecto');
        if (!confirm('¿Eliminar definitivamente el proyecto seleccionado? Esta accion borra en cascada.')) {
            return;
        }
        const respuesta = await api('/api/proyectos/' + id, { method: 'DELETE' });
        toast(respuesta.mensaje, 'success');
        marcarCatalogosDesactualizados();
    });

    $('#cd-proyecto').addEventListener('change', actualizarEquipoCambio);
    $('#rv-proyecto').addEventListener('change', actualizarEquipoRelevo);
    $('#ac-proyecto').addEventListener('change', prefillActualizarProyecto);

    actualizarEquipoCambio();
    actualizarEquipoRelevo();
}

async function actualizarEquipoCambio() {
    const id = valor('cd-proyecto');
    if (!id) {
        return;
    }
    try {
        const equipo = await api('/api/catalogos/proyectos/' + id + '/equipo');
        const activos = equipo.filter((miembro) => !miembro.resolucion_salida);
        const directores = activos.filter((miembro) => miembro.cargo === 'Director');
        $('#cd-saliente').innerHTML = opciones(directores, 'id_profesor', 'nombre', null, 'Sin director activo');
        $('#cd-equipo').innerHTML = '<div class="detail-box"><h4>Equipo actual</h4>' +
            tabla(equipo, ['nombre', 'cargo', 'resolucion_ingreso', 'resolucion_salida']) + '</div>';
    } catch (error) {
        $('#cd-equipo').innerHTML = errorBox(error.message);
    }
}

async function actualizarEquipoRelevo() {
    const id = valor('rv-proyecto');
    if (!id) {
        return;
    }
    try {
        const equipo = await api('/api/catalogos/proyectos/' + id + '/equipo');
        const activos = equipo.filter((miembro) => !miembro.resolucion_salida);
        const directores = activos.filter((miembro) => miembro.cargo === 'Director');
        $('#rv-saliente').innerHTML = opciones(directores, 'id_profesor', 'nombre', null, 'Sin director activo');
        $('#rv-equipo').innerHTML = '<div class="detail-box"><h4>Equipo actual</h4>' +
            tabla(equipo, ['nombre', 'cargo', 'resolucion_ingreso', 'resolucion_salida']) + '</div>';
    } catch (error) {
        $('#rv-equipo').innerHTML = errorBox(error.message);
    }
}

async function prefillActualizarProyecto() {
    const id = valor('ac-proyecto');
    if (!id) {
        return;
    }
    try {
        const detalle = await api('/api/proyectos/' + id);
        const proyecto = detalle.proyecto;
        if (!proyecto) {
            return;
        }
        $('#ac-nombre').value = proyecto.nombre_proyecto || '';
        $('#ac-acronimo').value = proyecto.acronimo || '';
        $('#ac-presupuesto').value = proyecto.presupuesto || '';
        $('#ac-programa').value = proyecto.programa || '';
        $('#ac-inicio').value = (proyecto.inicio_pro || '').toString().slice(0, 10);
        $('#ac-fin').value = (proyecto.fin_pro || '').toString().slice(0, 10);
        $('#ac-resolucion').value = proyecto.resolucion_consejo_uni_aprobado || '';
        $('#ac-descripcion').value = proyecto.descripcion || '';
    } catch (error) {
        toast(error.message, 'error');
    }
}

/*
 * BLOQUE: Productos
 * Alta y reclasificación usando el Factory Method del backend.
 */
async function renderProductos() {
    await cargarCatalogos();
    const puedeRegistrar = ['ADMIN', 'SECRETARIA'].includes(state.rol);
    const puedeListar = ['ADMIN', 'COORDINADOR', 'DIRECTOR'].includes(state.rol);
    const esAdmin = state.rol === 'ADMIN';
    let productos = [];

    if (puedeListar) {
        productos = await api('/api/productos');
    }

    let html = '<section class="hero-card"><p class="eyebrow">Patron creacional</p>' +
        '<h2>Productos cientificos</h2>' +
        '<p>El Factory Method construye la subclase correcta (Revista, Congreso, Libro o Patente) y el procedimiento ' +
        'transaccional guarda el producto en la tabla padre y en su tabla especializada.</p></section>';

    if (puedeRegistrar) {
        html += '<div class="grid-2">';
        html += tarjetaOperacion('Registrar producto', formularioProducto('prod', false), true);
        if (esAdmin) {
            html += tarjetaOperacion('Reclasificar producto', formularioProducto('rec', true));
        }
        html += '</div>';
    }

    if (puedeListar) {
        html += '<div class="card"><div class="card-head"><div><h2>Productos registrados</h2>' +
            '<p class="card-sub">Consulta directa de productos y su tabla especializada.</p></div></div>' +
            tabla(productos, ['id_producto', 'acronimo', 'num_secuencia', 'titulo_publicacion', 'descripcion_tipo', 'medio', 'detalle']) +
            '</div>';
    }

    setContenido(html);
    bindProductos(productos, puedeRegistrar, esAdmin);
}

function formularioProducto(prefijo, esReclasificacion) {
    const selectProducto = esReclasificacion
        ? '<div class="full"><label>Producto a reclasificar</label><select id="rec-producto"></select></div>'
        : '';

    return '<form id="' + prefijo + '-form">' +
        '<div class="form-grid">' +
        (esReclasificacion ? selectProducto : '<div class="full"><label>Proyecto</label>' + selectProyectos(prefijo + '-proyecto') + '</div>') +
        '<div><label>Tipo de producto</label><select id="' + prefijo + '-tipo">' +
        '<option value="1">Revista</option>' +
        '<option value="2">Congreso</option>' +
        '<option value="3">Libro</option>' +
        '<option value="4">Patente</option>' +
        '</select></div>' +
        '<div class="full"><label>Titulo de la publicacion</label><input id="' + prefijo + '-titulo" required></div>' +
        '</div>' +
        '<div id="' + prefijo + '-campos" class="form-grid" style="margin-top:12px"></div>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">' +
        (esReclasificacion ? 'Reclasificar producto' : 'Registrar producto') +
        '</button></div></form>';
}

function camposProducto(tipo, prefijo) {
    if (tipo === 1) {
        return campoTexto(prefijo, 'nombreRevista', 'Nombre de la revista', true) +
            campoTexto(prefijo, 'editorial', 'Editorial', true) +
            campoTexto(prefijo, 'volumen', 'Volumen', true) +
            campoSelect(prefijo, 'quartil', 'Quartil', ['', 'Q1', 'Q2', 'Q3', 'Q4'], false) +
            campoFecha(prefijo, 'fechaPublicacion', 'Fecha de publicacion', true);
    }
    if (tipo === 2) {
        return campoTexto(prefijo, 'nombreCongreso', 'Nombre del congreso', true) +
            campoTexto(prefijo, 'tipoCongreso', 'Tipo de congreso', true) +
            campoTexto(prefijo, 'lugarCelebracion', 'Lugar de celebracion', true) +
            campoTexto(prefijo, 'pais', 'Pais', true) +
            campoTexto(prefijo, 'tipoParticipacion', 'Tipo de participacion', true) +
            campoFecha(prefijo, 'fechaInicio', 'Fecha de inicio', true) +
            campoFecha(prefijo, 'fechaFin', 'Fecha de fin', true);
    }
    if (tipo === 3) {
        return campoTexto(prefijo, 'tituloLibro', 'Titulo del libro', true) +
            campoTexto(prefijo, 'editorial', 'Editorial', true) +
            campoTexto(prefijo, 'isbn', 'ISBN', true) +
            campoFecha(prefijo, 'fechaPublicacion', 'Fecha de publicacion', true);
    }
    return campoTexto(prefijo, 'nombrePatente', 'Nombre de la patente', true) +
        campoTexto(prefijo, 'numRegistro', 'Numero de registro', true) +
        campoTexto(prefijo, 'paisRegistro', 'Pais de registro', true) +
        campoTexto(prefijo, 'numSolicitud', 'Numero de solicitud', true) +
        campoFecha(prefijo, 'fechaSolicitud', 'Fecha de solicitud', true);
}

function campoTexto(prefijo, nombre, etiqueta, requerido) {
    return '<div><label>' + esc(etiqueta) + '</label><input id="' + prefijo + '-' + nombre + '"' +
        (requerido ? ' required' : '') + '></div>';
}

function campoFecha(prefijo, nombre, etiqueta, requerido) {
    return '<div><label>' + esc(etiqueta) + '</label><input type="date" id="' + prefijo + '-' + nombre + '"' +
        (requerido ? ' required' : '') + '></div>';
}

function campoSelect(prefijo, nombre, etiqueta, valores, requerido) {
    return '<div><label>' + esc(etiqueta) + '</label><select id="' + prefijo + '-' + nombre + '"' +
        (requerido ? ' required' : '') + '>' +
        valores.map((valor) => '<option value="' + esc(valor) + '">' + esc(valor || 'Sin valor') + '</option>').join('') +
        '</select></div>';
}

function bindProductos(productos, puedeRegistrar, esAdmin) {
    if (puedeRegistrar) {
        const tipoProd = $('#prod-tipo');
        $('#prod-campos').innerHTML = camposProducto(Number(tipoProd.value), 'prod');
        tipoProd.addEventListener('change', () => {
            $('#prod-campos').innerHTML = camposProducto(Number(tipoProd.value), 'prod');
        });

        enFormulario('prod-form', async () => {
            const datos = leerProducto('prod');
            datos.idProyecto = Number(valor('prod-proyecto'));
            const respuesta = await api('/api/productos', { method: 'POST', body: datos });
            toast(respuesta.mensaje, 'success');
            marcarCatalogosDesactualizados();
        });
    }

    if (esAdmin) {
        const select = $('#rec-producto');
        if (productos.length === 0) {
            select.innerHTML = '<option value="">No hay productos registrados</option>';
        } else {
            select.innerHTML = productos.map((producto) => (
                '<option value="' + producto.id_producto + '">' + esc(producto.acronimo) + ' - sec. ' +
                esc(producto.num_secuencia) + ' - ' + esc(producto.titulo_publicacion) + '</option>'
            )).join('');
        }

        const tipoRec = $('#rec-tipo');
        $('#rec-campos').innerHTML = camposProducto(Number(tipoRec.value), 'rec');
        tipoRec.addEventListener('change', () => {
            $('#rec-campos').innerHTML = camposProducto(Number(tipoRec.value), 'rec');
        });

        enFormulario('rec-form', async () => {
            const idProducto = valor('rec-producto');
            if (!idProducto) {
                throw new Error('No hay productos disponibles para reclasificar.');
            }
            const datos = leerProducto('rec');
            const respuesta = await api('/api/productos/' + idProducto + '/reclasificar', {
                method: 'POST',
                body: datos
            });
            toast(respuesta.mensaje, 'success');
            marcarCatalogosDesactualizados();
        });
    }
}

function leerProducto(prefijo) {
    const obtener = (nombre) => {
        const elemento = $('#' + prefijo + '-' + nombre);
        if (!elemento) {
            return null;
        }
        const texto = elemento.value.trim();
        return texto === '' ? null : texto;
    };

    return {
        tipo: Number(valor(prefijo + '-tipo')),
        titulo: obtener('titulo'),
        nombreRevista: obtener('nombreRevista'),
        editorial: obtener('editorial'),
        volumen: obtener('volumen'),
        quartil: obtener('quartil'),
        nombreCongreso: obtener('nombreCongreso'),
        tipoCongreso: obtener('tipoCongreso'),
        lugarCelebracion: obtener('lugarCelebracion'),
        pais: obtener('pais'),
        tipoParticipacion: obtener('tipoParticipacion'),
        isbn: obtener('isbn'),
        tituloLibro: obtener('tituloLibro'),
        numRegistro: obtener('numRegistro'),
        nombrePatente: obtener('nombrePatente'),
        paisRegistro: obtener('paisRegistro'),
        numSolicitud: obtener('numSolicitud'),
        fechaInicio: obtener('fechaInicio'),
        fechaFin: obtener('fechaFin'),
        fechaPublicacion: obtener('fechaPublicacion'),
        fechaSolicitud: obtener('fechaSolicitud')
    };
}

/*
 * BLOQUE: Reportes
 * Ejecuta los procedimientos analíticos y muestra resultados en tablas.
 */
async function renderReportes() {
    await cargarCatalogos();
    const permitidos = REPORTES.filter((reporte) => reporte.roles.includes(state.rol));

    let html = '<section class="hero-card"><p class="eyebrow">Consultas analiticas</p>' +
        '<h2>Reportes y consultas</h2>' +
        '<p>Los reportes se ejecutan con la conexion del usuario autenticado. Si el rol no tiene permiso EXECUTE ' +
        'sobre un procedimiento, MariaDB rechaza la operacion y el frontend muestra el error.</p></section>';

    html += '<div class="grid-2">';
    for (const reporte of permitidos) {
        html += '<div class="card"><div class="card-head"><div><h3>' + esc(reporte.titulo) + '</h3>' +
            '<p class="card-sub">' + esc(reporte.descripcion) + '</p></div></div>';
        if (reporte.id === 'ca5') {
            html += '<label>Dias de aviso</label><input id="ca5-dias" type="number" min="1" max="365" value="30">';
        }
        if (reporte.id === 'cm1') {
            html += '<label>Proyecto</label>' + selectProyectos('cm1-proyecto', true);
        }
        html += '<div class="form-actions"><button type="button" class="btn btn-primary" data-reporte="' +
            reporte.id + '">Ejecutar</button></div></div>';
    }
    html += '</div><div id="reporte-resultado" class="result-panel"></div>';

    setContenido(html);
    $$('[data-reporte]').forEach((boton) => {
        boton.addEventListener('click', () => ejecutarReporte(boton.dataset.reporte));
    });
}

async function ejecutarReporte(id) {
    const definicion = REPORTES.find((reporte) => reporte.id === id);
    if (!definicion) {
        return;
    }

    let url = definicion.url;
    if (id === 'ca5') {
        url += '?dias=' + encodeURIComponent(valor('ca5-dias') || '30');
    }
    if (id === 'cm1') {
        const proyecto = valor('cm1-proyecto');
        if (proyecto) {
            url += '?idProyecto=' + encodeURIComponent(proyecto);
        }
    }

    const panel = $('#reporte-resultado');
    panel.innerHTML = cargando('Ejecutando ' + definicion.titulo + '...');
    try {
        const datos = await api(url);
        panel.innerHTML = '<div class="card"><div class="card-head"><div><h2>' + esc(definicion.titulo) + '</h2>' +
            '<p class="card-sub">' + esc(definicion.descripcion) + '</p></div>' +
            '<span class="pill">' + datos.length + ' fila(s)</span></div>' +
            tabla(datos) + '</div>';
    } catch (error) {
        panel.innerHTML = '<div class="card">' + errorBox(error.message) + '</div>';
    }
}

/*
 * BLOQUE: Espacio docente
 * Solo muestra los proyectos, productos y equipo del docente vinculado a la cuenta.
 */
async function renderProfesor() {
    const [proyectos, productos, equipo] = await Promise.all([
        api('/api/reportes/vista-docente'),
        api('/api/reportes/ca3'),
        api('/api/reportes/cm1')
    ]);

    const html = '<section class="hero-card"><p class="eyebrow">Datos propios</p>' +
        '<h2>Hola, ' + esc(state.nombrePersona || state.usuario) + '</h2>' +
        '<p>Aquí solo aparecen los proyectos en los que participas, tus productos y el equipo de esos proyectos. ' +
        'No es posible consultar información de otros docentes ni de proyectos en los que no estés involucrado.</p></section>' +
        '<div class="grid-3">' +
        tarjetaResumen('Mis proyectos activos', proyectos.length) +
        tarjetaResumen('Mis productos', productos.length) +
        tarjetaResumen('Compañeros de equipo', new Set(equipo.map((fila) => fila.nombre)).size) +
        '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Mis proyectos</h3>' +
        '<p class="card-sub">Proyectos vigentes en los que participo.</p></div>' +
        '<span class="pill">' + proyectos.length + ' proyecto(s)</span></div>' +
        tabla(proyectos, ['acronimo', 'nombre_proyecto', 'cargo', 'fin_pro', 'prorroga']) + '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Mis productos científicos</h3>' +
        '<p class="card-sub">Productos registrados en mis proyectos.</p></div>' +
        '<span class="pill">' + productos.length + ' producto(s)</span></div>' +
        tabla(productos, ['acronimo', 'num_secuencia', 'titulo_publicacion', 'tipo']) + '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Equipo de mis proyectos</h3>' +
        '<p class="card-sub">Docentes activos de los proyectos en los que participo.</p></div></div>' +
        tabla(equipo, ['acronimo', 'cargo', 'nombre', 'telefono']) + '</div>';

    setContenido(html);
}

/*
 * BLOQUE: Espacio estudiante
 * Solo muestra los proyectos y productos del estudiante vinculado a la cuenta.
 */
async function renderEstudiante() {
    const [proyectos, productos] = await Promise.all([
        api('/api/reportes/vista-estudiante'),
        api('/api/reportes/cm5')
    ]);

    const html = '<section class="hero-card"><p class="eyebrow">Datos propios</p>' +
        '<h2>Hola, ' + esc(state.nombrePersona || state.usuario) + '</h2>' +
        '<p>Aquí solo aparecen los proyectos a los que estás vinculado y los productos de esos proyectos. ' +
        'No es posible consultar información de otros estudiantes ni de proyectos ajenos.</p></section>' +
        '<div class="grid-3">' +
        tarjetaResumen('Mis proyectos', proyectos.length) +
        tarjetaResumen('Productos de mis proyectos', productos.length) +
        '<div class="card"><h3>Mi información</h3><p class="muted">' +
        esc(state.nombrePersona || state.usuario) + '</p></div>' +
        '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Mis proyectos</h3>' +
        '<p class="card-sub">Proyectos a los que pertenezco.</p></div>' +
        '<span class="pill">' + proyectos.length + ' proyecto(s)</span></div>' +
        tabla(proyectos, ['acronimo', 'nombre_proyecto', 'departamento_facultad', 'inicio_pro', 'fin_pro']) + '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Productos de mis proyectos</h3>' +
        '<p class="card-sub">Producción científica de los proyectos en los que participo.</p></div>' +
        '<span class="pill">' + productos.length + ' producto(s)</span></div>' +
        tabla(productos, ['acronimo', 'sec', 'titulo', 'tipo', 'medio']) + '</div>';

    setContenido(html);
}

/*
 * BLOQUE: Gestión de usuarios
 * Admin crea secretaría/coordinador; secretaría crea profesor/estudiante.
 */
async function renderUsuarios() {
    await cargarCatalogos();
    const esAdmin = state.rol === 'ADMIN';
    const roles = esAdmin ? ['SECRETARIA', 'COORDINADOR'] : ['PROFESOR', 'ESTUDIANTE'];
    const usuarios = await api('/api/usuarios');

    const html = '<section class="hero-card"><p class="eyebrow">Seguridad</p>' +
        '<h2>Gestion de usuarios</h2><p>' +
        (esAdmin
            ? 'Como administrador puede crear cuentas de secretaria y coordinador.'
            : 'Como secretaria puede crear cuentas de profesor y estudiante vinculadas a una persona real.') +
        '</p></section>' +
        '<div class="grid-2">' +
        '<div class="card"><h3>Nuevo usuario</h3><form id="usr-form"><div class="form-grid">' +
        campoTexto('usr', 'usuario', 'Usuario', true) +
        campoTexto('usr', 'password', 'Contrasena (minimo 6 caracteres)', true) +
        campoSelect('usr', 'rol', 'Rol', roles, true) +
        '</div><div id="usr-extra" class="form-grid" style="margin-top:12px"></div>' +
        '<div class="form-actions"><button type="submit" class="btn btn-primary">Crear usuario</button></div></form></div>' +
        '<div class="card"><div class="card-head"><div><h3>Usuarios registrados</h3>' +
        '<p class="card-sub">Creados por este rol.</p></div><span class="pill">' + usuarios.length + '</span></div>' +
        tabla(usuarios, ['usuario', 'rol', 'nombre_persona', 'id_profesor', 'id_estudiante']) + '</div></div>';

    setContenido(html);

    function extraSegunRol() {
        const rol = valor('usr-rol');
        $('#usr-extra').innerHTML = rol === 'PROFESOR'
            ? '<div class="full"><label>Profesor</label>' + selectProfesores('usr-idProfesor') + '</div>'
            : rol === 'ESTUDIANTE'
                ? '<div class="full"><label>Estudiante</label>' + selectEstudiantes('usr-idEstudiante') + '</div>'
                : campoTexto('usr', 'nombrePersona', 'Nombre de la persona', true);
    }

    extraSegunRol();
    $('#usr-rol').addEventListener('change', extraSegunRol);

    enFormulario('usr-form', async () => {
        const rol = valor('usr-rol');
        const datos = {
            usuario: valor('usr-usuario'),
            password: valor('usr-password'),
            rol: rol,
            idProfesor: rol === 'PROFESOR' ? Number(valor('usr-idProfesor')) : null,
            idEstudiante: rol === 'ESTUDIANTE' ? Number(valor('usr-idEstudiante')) : null,
            nombrePersona: ['PROFESOR', 'ESTUDIANTE'].includes(rol) ? null : valor('usr-nombrePersona')
        };
        const respuesta = await api('/api/usuarios', { method: 'POST', body: datos });
        toast(respuesta.mensaje, 'success');
        await renderUsuarios();
    });
}

/*
 * BLOQUE: Auditoría
 * Muestra las tablas generadas por los triggers de base de datos.
 */
async function renderAuditoria() {
    const [operaciones, proyectos, participacion] = await Promise.all([
        api('/api/auditoria/operaciones'),
        api('/api/auditoria/proyectos'),
        api('/api/auditoria/participacion')
    ]);

    const html = '<section class="hero-card"><p class="eyebrow">Trazabilidad</p>' +
        '<h2>Auditoria y triggers</h2>' +
        '<p>Estas tablas se llenan automaticamente mediante los triggers del script SQL: ' +
        'trg_auditoria_proyecto_upd, trg_auditoria_proyecto_del y trg_historial_participacion.</p></section>' +
        '<div class="card"><div class="card-head"><div><h3>Auditoria de operaciones</h3>' +
        '<p class="card-sub">Resultado de procedimientos transaccionales.</p></div></div>' +
        tabla(operaciones, ['fecha', 'operacion', 'procedimiento_ejecutado', 'resultado']) + '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Auditoria de proyectos</h3>' +
        '<p class="card-sub">Cambios y eliminaciones sobre proyecto_investigacion.</p></div></div>' +
        tabla(proyectos, ['id_proyecto', 'operacion', 'usuario', 'fecha_hora', 'datos_anteriores', 'datos_nuevos']) + '</div>' +
        '<div class="card"><div class="card-head"><div><h3>Historial de participacion</h3>' +
        '<p class="card-sub">Cambios de cargo o desvinculacion de docentes.</p></div></div>' +
        tabla(participacion, ['id_proyecto', 'id_profesor', 'cargo_anterior', 'cargo_nuevo', 'resolucion_salida', 'motivo_salida', 'usuario', 'fecha_cambio']) + '</div>';

    setContenido(html);
}

/*
 * BLOQUE: Utilidades
 * Catálogos en caché, selects, formato de tablas, escape HTML y cliente fetch.
 */
async function cargarCatalogos() {
    if (state.catalogos.cargados) {
        return state.catalogos;
    }

    const [proyectos, profesores, estudiantes, lineas, tipos] = await Promise.all([
        api('/api/catalogos/proyectos'),
        api('/api/catalogos/profesores'),
        api('/api/catalogos/estudiantes'),
        api('/api/catalogos/lineas'),
        api('/api/catalogos/tipos-producto')
    ]);

    state.catalogos = {
        cargados: true,
        proyectos,
        profesores,
        estudiantes,
        lineas,
        tipos
    };
    return state.catalogos;
}

function marcarCatalogosDesactualizados() {
    state.catalogos.cargados = false;
}

function selectProyectos(id, incluirTodos = false) {
    const opcionesProyecto = state.catalogos.proyectos.map((proyecto) => (
        '<option value="' + proyecto.id + '">' + esc(proyecto.acronimo) + ' - ' + esc(proyecto.nombre) + '</option>'
    )).join('');
    const primera = incluirTodos ? '<option value="">Todos los proyectos</option>' : '';
    return '<select id="' + id + '">' + primera + opcionesProyecto + '</select>';
}

function selectProfesores(id) {
    const opcionesProfesor = state.catalogos.profesores.map((profesor) => (
        '<option value="' + profesor.id + '">' + esc(profesor.nombre) + '</option>'
    )).join('');
    return '<select id="' + id + '">' + opcionesProfesor + '</select>';
}

function selectLineas(id) {
    const opcionesLinea = state.catalogos.lineas.map((linea) => (
        '<option value="' + linea.id + '">' + esc(linea.nombre) + '</option>'
    )).join('');
    return '<select id="' + id + '">' + opcionesLinea + '</select>';
}

function selectEstudiantes(id) {
    const opcionesEstudiante = state.catalogos.estudiantes.map((estudiante) => (
        '<option value="' + estudiante.id + '">' + esc(estudiante.nombre) + ' - ' + esc(estudiante.acronimo) + '</option>'
    )).join('');
    return '<select id="' + id + '">' + opcionesEstudiante + '</select>';
}

function opciones(items, valorClave, etiquetaClave, seleccionado, vacio) {
    if (!items || items.length === 0) {
        return '<option value="">' + esc(vacio || 'Sin opciones disponibles') + '</option>';
    }
    return items.map((item) => (
        '<option value="' + esc(item[valorClave]) + '"' +
        (String(item[valorClave]) === String(seleccionado) ? ' selected' : '') + '>' +
        esc(item[etiquetaClave]) + '</option>'
    )).join('');
}

function enFormulario(id, manejador) {
    const formulario = $('#' + id);
    if (!formulario) {
        return;
    }
    formulario.addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await manejador(formulario);
        } catch (error) {
            toast(error.message, 'error');
        }
    });
}

function valor(id) {
    const elemento = $('#' + id);
    return elemento ? elemento.value.trim() : '';
}

function badgeEstado(estado) {
    const clases = {
        'Cerrado': 'badge-gray',
        'Vencido sin cierre': 'badge-red',
        'Por finalizar': 'badge-amber',
        'En ejecucion': 'badge-green'
    };
    return '<span class="badge ' + (clases[estado] || 'badge-blue') + '">' + esc(estado) + '</span>';
}

function estadoProyecto(proyecto) {
    if (proyecto.resolucion_consejo_uni_cierre) {
        return 'Cerrado';
    }
    const fin = (proyecto.prorroga || proyecto.fin_pro || '').toString().slice(0, 10);
    const hoy = new Date().toISOString().slice(0, 10);
    if (fin && fin < hoy) {
        return 'Vencido sin cierre';
    }
    return 'En ejecucion';
}

function tabla(datos, columnas) {
    if (!Array.isArray(datos) || datos.length === 0) {
        return '<div class="empty">Sin registros para mostrar.</div>';
    }
    const cabeceras = columnas || Object.keys(datos[0]);
    return '<div class="table-wrap"><table class="tabla"><thead><tr>' +
        cabeceras.map((columna) => '<th>' + esc(tituloColumna(columna)) + '</th>').join('') +
        '</tr></thead><tbody>' +
        datos.map((fila) => '<tr>' +
            cabeceras.map((columna) => '<td>' + esc(formatear(fila[columna])) + '</td>').join('') +
            '</tr>').join('') +
        '</tbody></table></div>';
}

function tituloColumna(columna) {
    return String(columna).replace(/_/g, ' ').replace(/\b\w/g, (letra) => letra.toUpperCase());
}

function formatear(valorDato) {
    if (valorDato === null || valorDato === undefined || valorDato === '') {
        return '-';
    }
    if (typeof valorDato === 'boolean') {
        return valorDato ? 'Si' : 'No';
    }
    if (typeof valorDato === 'number') {
        return valorDato.toLocaleString('es-EC');
    }
    if (typeof valorDato === 'string') {
        if (/^\d{4}-\d{2}-\d{2}$/.test(valorDato)) {
            const partes = valorDato.split('-');
            return partes[2] + '/' + partes[1] + '/' + partes[0];
        }
        if (/^\d{4}-\d{2}-\d{2}T/.test(valorDato)) {
            const partes = valorDato.slice(0, 10).split('-');
            return partes[2] + '/' + partes[1] + '/' + partes[0] + ' ' + valorDato.slice(11, 16);
        }
        return valorDato;
    }
    return JSON.stringify(valorDato);
}

function esc(valorDato) {
    return String(valorDato === null || valorDato === undefined ? '' : valorDato)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function cargando(mensaje = 'Cargando...') {
    return '<div class="loading"><span class="spinner"></span>' + esc(mensaje) + '</div>';
}

function errorBox(mensaje) {
    return '<div class="alert alert-error">' + esc(mensaje) + '</div>';
}

function setContenido(html) {
    $('#contenido').innerHTML = html;
}

function toast(mensaje, tipo = 'success') {
    const elemento = $('#toast');
    elemento.textContent = mensaje;
    elemento.className = 'toast ' + tipo;
    clearTimeout(toast.temporizador);
    toast.temporizador = setTimeout(() => {
        elemento.className = 'toast hidden';
    }, 4500);
}

async function api(url, opciones = {}) {
    const configuracion = {
        credentials: 'same-origin',
        ...opciones,
        headers: { ...(opciones.headers || {}) }
    };

    if (configuracion.body && typeof configuracion.body !== 'string') {
        configuracion.body = JSON.stringify(configuracion.body);
        configuracion.headers['Content-Type'] = 'application/json';
    }

    const respuesta = await fetch(url, configuracion);
    const texto = await respuesta.text();
    let datos = null;
    if (texto) {
        try {
            datos = JSON.parse(texto);
        } catch (error) {
            datos = texto;
        }
    }

    if (respuesta.status === 401) {
        mostrarLogin();
        throw new Error((datos && datos.mensaje) || 'La sesion expiro. Inicie sesion nuevamente.');
    }

    if (!respuesta.ok) {
        const mensaje = (datos && (datos.mensaje || datos.error)) || ('Error HTTP ' + respuesta.status);
        throw new Error(mensaje);
    }

    return datos;
}
