import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RolService, RolResponse } from '../../core/services/rol.service';
import { PrivilegioService, PrivilegioResponseDTO } from '../../core/services/privilegio.service';
import { UsuarioService, Usuario } from '../../core/services/usuario.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { SearchBarComponent } from '../../shared/search-bar/search-bar.component';
import { coincide } from '../../core/utils/search.util';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, SearchBarComponent],
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.css'
})
export class RolesComponent implements OnInit {
  roles: RolResponse[] = [];
  privilegios: PrivilegioResponseDTO[] = [];
  usuarios: Usuario[] = [];
  busqueda = '';

  rolSeleccionado: RolResponse | null = null;
  cargando = false;
  exito = '';
  error = '';

  mostrarModalRol = false;
  mostrarModalPrivilegios = false;
  mostrarModalUsuario = false;
  modoEdicion = false;
  rolEditandoId: number | null = null;
  guardando = false;

  privilegiosSeleccionados: Set<number> = new Set();
  usuarioRolId: number | null = null;

  formRol: FormGroup;

  constructor(
    private rolService: RolService,
    private privilegioService: PrivilegioService,
    private usuarioService: UsuarioService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private confirm: ConfirmService
  ) {
    this.formRol = this.fb.group({
      nombreRol: ['', Validators.required],
      descripcionRol: ['']
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  get rolesFiltrados(): RolResponse[] {
    return this.roles.filter(r => coincide(this.busqueda, r.nombreRol, r.descripcionRol));
  }

  cargar(): void {
    this.cargando = true;
    this.rolService.listar().subscribe({
      next: (data) => { this.roles = data; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Error al cargar roles.'; this.cargando = false; }
    });
    this.privilegioService.listar().subscribe({ next: (d) => { this.privilegios = d; this.cdr.detectChanges(); } });
    this.usuarioService.listar().subscribe({ next: (d) => { this.usuarios = d; this.cdr.detectChanges(); } });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.rolEditandoId = null;
    this.formRol.reset();
    this.mostrarModalRol = true;
  }

  abrirEditar(rol: RolResponse): void {
    this.modoEdicion = true;
    this.rolEditandoId = rol.idRol;
    this.formRol.patchValue({ nombreRol: rol.nombreRol, descripcionRol: rol.descripcionRol });
    this.mostrarModalRol = true;
  }

  guardarRol(): void {
    if (this.formRol.invalid || this.guardando) return;
    this.guardando = true;
    const dto = this.formRol.value;
    const op = this.modoEdicion && this.rolEditandoId
      ? this.rolService.actualizar(this.rolEditandoId, dto)
      : this.rolService.crear(dto);
    op.subscribe({
      next: () => { this.mostrarExito('Rol guardado.'); this.mostrarModalRol = false; this.cargar(); },
      error: (e) => { this.error = e.error?.mensaje || 'Error al guardar.'; this.guardando = false; }
    });
  }

  async eliminarRol(id: number): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Eliminar rol',
      message: 'Se eliminará el rol y sus asignaciones. ¿Continuar?',
      confirmText: 'Eliminar',
      variant: 'danger'
    });
    if (!ok) return;
    this.rolService.eliminar(id).subscribe({
      next: () => { this.mostrarExito('Rol eliminado.'); this.cargar(); },
      error: () => this.mostrarError('Error al eliminar.')
    });
  }

  abrirPrivilegios(rol: RolResponse): void {
    this.rolSeleccionado = rol;
    this.privilegiosSeleccionados = new Set(rol.privilegios.map(p => p.idPrivilegio));
    this.mostrarModalPrivilegios = true;
  }

  togglePrivilegio(id: number): void {
    this.privilegiosSeleccionados.has(id)
      ? this.privilegiosSeleccionados.delete(id)
      : this.privilegiosSeleccionados.add(id);
  }

  guardarPrivilegios(): void {
    if (!this.rolSeleccionado) return;
    this.guardando = true;
    this.rolService.asignarPrivilegios(this.rolSeleccionado.idRol, [...this.privilegiosSeleccionados]).subscribe({
      next: () => { this.mostrarExito('Privilegios actualizados.'); this.mostrarModalPrivilegios = false; this.cargar(); },
      error: () => this.mostrarError('Error al guardar privilegios.')
    });
  }

  abrirAsignarUsuario(rol: RolResponse): void {
    this.rolSeleccionado = rol;
    this.usuarioRolId = null;
    this.mostrarModalUsuario = true;
  }

  asignarRolAUsuario(): void {
    if (!this.rolSeleccionado || !this.usuarioRolId) return;
    this.guardando = true;
    this.rolService.asignarRolAUsuario(this.rolSeleccionado.idRol, this.usuarioRolId).subscribe({
      next: () => { this.mostrarExito('Rol asignado al usuario.'); this.mostrarModalUsuario = false; this.cargar(); },
      error: (e) => { this.error = e.error?.mensaje || 'Error al asignar.'; this.guardando = false; }
    });
  }

  tienePrivilegio(id: number): boolean {
    return this.privilegiosSeleccionados.has(id);
  }

  private mostrarExito(msg: string): void {
    this.exito = msg; this.guardando = false;
    setTimeout(() => this.exito = '', 3500);
  }

  private mostrarError(msg: string): void {
    this.error = msg;
    setTimeout(() => this.error = '', 3500);
  }
}
