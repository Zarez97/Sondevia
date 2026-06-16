import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UsuarioService, Usuario, CrearUsuario, ActualizarUsuario } from '../../core/services/usuario.service';
import { RolService, RolResponse } from '../../core/services/rol.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { AuthService } from '../../core/services/auth.service';
import { SearchBarComponent } from '../../shared/search-bar/search-bar.component';
import { coincide } from '../../core/utils/search.util';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SearchBarComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  cargando = false;
  error = '';
  exito = '';

  mostrarModal = false;
  modoEdicion = false;
  usuarioEditandoId: number | null = null;
  guardando = false;

  form: FormGroup;

  readonly ACTIVO = 1;
  readonly INACTIVO = 0;
  readonly BLOQUEADO = 2;

  soloBloqueados = false;
  busqueda = '';

  // Gestión de roles del usuario
  mostrarRolesModal = false;
  usuarioRoles: Usuario | null = null;
  rolesDisponibles: RolResponse[] = [];
  rolesSeleccionados = new Set<number>();
  private rolesOriginales = new Set<number>();
  guardandoRoles = false;

  constructor(
    private usuarioService: UsuarioService,
    private rolService: RolService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private confirm: ConfirmService,
    private auth: AuthService
  ) {
    this.form = this.fb.group({
      nombreUser: ['', Validators.required],
      emailUser: ['', [Validators.required, Validators.email]],
      contraseniaUser: ['', Validators.required],
      fechaNacimiento: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.soloBloqueados = this.route.snapshot.routeConfig?.path === 'usuarios/bloqueados';
    this.cargarUsuarios();
  }

  get listaVisible(): Usuario[] {
    const base = this.soloBloqueados
      ? this.usuarios.filter(u => u.estadoUser === this.BLOQUEADO)
      : this.usuarios;
    return base.filter(u =>
      coincide(this.busqueda, u.nombreUser, u.emailUser, u.roles?.join(' '), this.estadoLabel(u.estadoUser))
    );
  }

  get hayUsuarios(): boolean {
    return this.soloBloqueados
      ? this.usuarios.some(u => u.estadoUser === this.BLOQUEADO)
      : this.usuarios.length > 0;
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.listar().subscribe({
      next: (data) => { this.usuarios = data; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Error al cargar usuarios.'; this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.usuarioEditandoId = null;
    this.form.reset();
    this.form.get('contraseniaUser')?.setValidators(Validators.required);
    this.form.get('emailUser')?.enable();
    this.mostrarModal = true;
  }

  abrirEditar(u: Usuario): void {
    this.modoEdicion = true;
    this.usuarioEditandoId = u.idUser;
    this.form.patchValue({
      nombreUser: u.nombreUser,
      fechaNacimiento: u.fechaNacimiento,
      emailUser: u.emailUser,
      contraseniaUser: ''
    });
    this.form.get('contraseniaUser')?.clearValidators();
    this.form.get('emailUser')?.disable();
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.error = '';
  }

  guardar(): void {
    if (this.form.invalid || this.guardando) return;
    this.guardando = true;
    this.error = '';

    if (this.modoEdicion && this.usuarioEditandoId) {
      const dto: ActualizarUsuario = {
        nombreUser: this.form.value.nombreUser,
        fechaNacimiento: this.form.value.fechaNacimiento
      };
      this.usuarioService.actualizar(this.usuarioEditandoId, dto).subscribe({
        next: () => { this.mostrarExito('Usuario actualizado.'); this.cerrarModal(); this.cargarUsuarios(); },
        error: (e) => { this.error = e.error?.mensaje || 'Error al actualizar.'; this.guardando = false; this.cdr.detectChanges(); }
      });
    } else {
      const dto: CrearUsuario = this.form.value;
      this.usuarioService.crear(dto).subscribe({
        next: () => { this.mostrarExito('Usuario creado correctamente.'); this.cerrarModal(); this.cargarUsuarios(); },
        error: (e) => { this.error = e.error?.mensaje || 'Error al crear usuario.'; this.guardando = false; this.cdr.detectChanges(); }
      });
    }
  }

  activar(id: number): void {
    this.usuarioService.activar(id).subscribe({
      next: () => { this.mostrarExito('Usuario activado.'); this.cargarUsuarios(); },
      error: () => this.mostrarError('Error al activar usuario.')
    });
  }

  async darDeBaja(id: number): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Dar de baja usuario',
      message: 'El usuario quedará inactivo y no podrá iniciar sesión. ¿Continuar?',
      confirmText: 'Dar de baja',
      variant: 'danger'
    });
    if (!ok) return;
    this.usuarioService.darDeBaja(id).subscribe({
      next: () => { this.mostrarExito('Usuario dado de baja.'); this.cargarUsuarios(); },
      error: () => this.mostrarError('Error al dar de baja.')
    });
  }

  desbloquear(id: number): void {
    this.usuarioService.desbloquear(id).subscribe({
      next: () => { this.mostrarExito('Usuario desbloqueado. Se envió notificación por correo.'); this.cargarUsuarios(); },
      error: () => this.mostrarError('Error al desbloquear usuario.')
    });
  }

  async eliminar(u: Usuario): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Eliminar usuario',
      message: `Se eliminará permanentemente a "${u.nombreUser}" (${u.emailUser}). Esta acción no se puede deshacer. ¿Continuar?`,
      confirmText: 'Eliminar',
      variant: 'danger'
    });
    if (!ok) return;
    this.usuarioService.eliminar(u.idUser).subscribe({
      next: () => { this.mostrarExito('Usuario eliminado.'); this.cargarUsuarios(); },
      error: (e) => this.manejarErrorEliminar(e, u)
    });
  }

  // Si no se pudo eliminar por dependencias, ofrece dar de baja en el mismo diálogo
  private async manejarErrorEliminar(e: any, u: Usuario): Promise<void> {
    const mensaje = e.error?.mensaje || 'No se pudo eliminar el usuario.';
    const ofrecerBaja = e.status === 409 && u.estadoUser === this.ACTIVO && !this.esUsuarioActual(u);

    if (!ofrecerBaja) { this.mostrarError(mensaje); return; }

    const baja = await this.confirm.ask({
      title: 'No se puede eliminar',
      message: `${mensaje} ¿Quieres darlo de baja ahora? Quedará inactivo y conservará su historial.`,
      confirmText: 'Dar de baja',
      cancelText: 'Cerrar',
      variant: 'warning'
    });
    if (!baja) return;
    this.usuarioService.darDeBaja(u.idUser).subscribe({
      next: () => { this.mostrarExito('Usuario dado de baja.'); this.cargarUsuarios(); },
      error: () => this.mostrarError('Error al dar de baja.')
    });
  }

  private esUsuarioActual(u: Usuario): boolean {
    return this.auth.getUser()?.email?.toLowerCase() === u.emailUser.toLowerCase();
  }

  // ── Gestión de roles del usuario ──────────────────────
  abrirRoles(u: Usuario): void {
    this.usuarioRoles = u;
    this.error = '';
    this.guardandoRoles = false;
    this.rolesDisponibles = [];
    this.mostrarRolesModal = true;
    this.rolService.listar().subscribe({
      next: (roles) => {
        this.rolesDisponibles = roles;
        const actuales = roles.filter(r => u.roles.includes(r.nombreRol)).map(r => r.idRol);
        this.rolesOriginales = new Set(actuales);
        this.rolesSeleccionados = new Set(actuales);
        this.cdr.detectChanges();
      },
      error: () => {
        this.mostrarRolesModal = false;
        this.mostrarError('No se pudieron cargar los roles.');
      }
    });
  }

  toggleRol(rolId: number): void {
    if (this.rolesSeleccionados.has(rolId)) this.rolesSeleccionados.delete(rolId);
    else this.rolesSeleccionados.add(rolId);
  }

  cerrarRoles(): void {
    this.mostrarRolesModal = false;
    this.usuarioRoles = null;
  }

  guardarRoles(): void {
    if (!this.usuarioRoles || this.guardandoRoles) return;
    const userId = this.usuarioRoles.idUser;
    const aAgregar = [...this.rolesSeleccionados].filter(id => !this.rolesOriginales.has(id));
    const aQuitar = [...this.rolesOriginales].filter(id => !this.rolesSeleccionados.has(id));

    if (aAgregar.length === 0 && aQuitar.length === 0) {
      this.cerrarRoles();
      return;
    }

    this.guardandoRoles = true;
    this.error = '';
    const ops = [
      ...aAgregar.map(id => this.rolService.asignarRolAUsuario(id, userId)),
      ...aQuitar.map(id => this.rolService.quitarRolAUsuario(id, userId))
    ];
    forkJoin(ops).subscribe({
      next: () => {
        this.guardandoRoles = false;
        this.cerrarRoles();
        this.mostrarExito('Roles actualizados correctamente.');
        this.cargarUsuarios();
      },
      error: () => {
        this.guardandoRoles = false;
        this.error = 'No se pudieron actualizar los roles.';
        this.cdr.detectChanges();
      }
    });
  }

  estadoLabel(estado: number): string {
    if (estado === this.ACTIVO) return 'Activo';
    if (estado === this.INACTIVO) return 'Inactivo';
    if (estado === this.BLOQUEADO) return 'Bloqueado';
    return 'Desconocido';
  }

  private mostrarExito(msg: string): void {
    this.exito = msg;
    this.guardando = false;
    this.cdr.detectChanges();
    setTimeout(() => { this.exito = ''; this.cdr.detectChanges(); }, 3500);
  }

  private mostrarError(msg: string): void {
    this.error = msg;
    this.cdr.detectChanges();
    setTimeout(() => { this.error = ''; this.cdr.detectChanges(); }, 3500);
  }
}
