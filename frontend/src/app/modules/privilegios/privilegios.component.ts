import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PrivilegioService, PrivilegioResponseDTO } from '../../core/services/privilegio.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { SearchBarComponent } from '../../shared/search-bar/search-bar.component';
import { coincide } from '../../core/utils/search.util';

@Component({
  selector: 'app-privilegios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SearchBarComponent],
  templateUrl: './privilegios.component.html',
  styleUrl: './privilegios.component.css'
})
export class PrivilegiosComponent implements OnInit {
  privilegios: PrivilegioResponseDTO[] = [];
  busqueda = '';
  cargando = false;
  exito = '';
  error = '';
  mostrarModal = false;
  modoEdicion = false;
  editandoId: number | null = null;
  guardando = false;

  form: FormGroup;

  constructor(
    private privilegioService: PrivilegioService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private confirm: ConfirmService
  ) {
    this.form = this.fb.group({
      nombrePrivilegio: ['', Validators.required],
      descripcionPrivilegio: [''],
      urlPrivilegio: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  get privilegiosFiltrados(): PrivilegioResponseDTO[] {
    return this.privilegios.filter(p =>
      coincide(this.busqueda, p.nombrePrivilegio, p.descripcionPrivilegio, p.urlPrivilegio)
    );
  }

  cargar(): void {
    this.cargando = true;
    this.privilegioService.listar().subscribe({
      next: (data) => { this.privilegios = data; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Error al cargar privilegios.'; this.cargando = false; }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.editandoId = null;
    this.form.reset();
    this.mostrarModal = true;
  }

  abrirEditar(p: PrivilegioResponseDTO): void {
    this.modoEdicion = true;
    this.editandoId = p.idPrivilegio;
    this.form.patchValue({
      nombrePrivilegio: p.nombrePrivilegio,
      descripcionPrivilegio: p.descripcionPrivilegio,
      urlPrivilegio: p.urlPrivilegio
    });
    this.mostrarModal = true;
  }

  guardar(): void {
    if (this.form.invalid || this.guardando) return;
    this.guardando = true;
    this.error = '';
    const dto = this.form.value;
    const op = this.modoEdicion && this.editandoId
      ? this.privilegioService.actualizar(this.editandoId, dto)
      : this.privilegioService.crear(dto);
    op.subscribe({
      next: () => { this.mostrarExito('Privilegio guardado.'); this.mostrarModal = false; this.cargar(); },
      error: (e) => { this.error = e.error?.mensaje || 'Error al guardar.'; this.guardando = false; }
    });
  }

  async eliminar(id: number): Promise<void> {
    const ok = await this.confirm.ask({
      title: 'Eliminar privilegio',
      message: 'Se eliminará el privilegio y se quitará de los roles que lo tengan. ¿Continuar?',
      confirmText: 'Eliminar',
      variant: 'danger'
    });
    if (!ok) return;
    this.privilegioService.eliminar(id).subscribe({
      next: () => { this.mostrarExito('Privilegio eliminado.'); this.cargar(); },
      error: () => this.mostrarError('Error al eliminar.')
    });
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
