import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  errorMessage = '';
  showPassword = false;
  redirectUrl: string | null = null;
  bloqueado = false;       // la cuenta quedó bloqueada por intentos fallidos
  solicitando = false;     // envío de solicitud de desbloqueo en curso

  togglePassword(): void { this.showPassword = !this.showPassword; }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private confirm: ConfirmService
  ) {
    this.redirectUrl = this.route.snapshot.queryParamMap.get('redirect');
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      contrasenia: ['', Validators.required]
    });
  }

  get registroQueryParams() {
    return this.redirectUrl ? { redirect: this.redirectUrl } : {};
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;

    this.loading = true;
    this.errorMessage = '';
    this.bloqueado = false;

    this.authService.login(this.form.value).subscribe({
      next: () => this.router.navigateByUrl(this.redirectUrl || '/dashboard'),
      error: (e) => {
        this.errorMessage = e.error?.mensaje || 'Correo o contraseña incorrectos.';
        this.bloqueado = e.status === 423; // cuenta bloqueada
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  async solicitarDesbloqueo(): Promise<void> {
    const email = this.form.get('email')?.value;
    if (!email || this.form.get('email')?.invalid || this.solicitando) return;

    const ok = await this.confirm.ask({
      title: 'Solicitar desbloqueo',
      message: `Se enviará una solicitud al administrador para desbloquear la cuenta "${email}". ¿Deseas continuar?`,
      confirmText: 'Enviar solicitud',
      variant: 'primary'
    });
    if (!ok) return;

    this.solicitando = true;
    this.authService.solicitarDesbloqueo(email).subscribe({
      next: (res) => {
        this.solicitando = false;
        this.confirm.alert({
          title: 'Solicitud enviada',
          message: res.mensaje || 'Tu solicitud fue enviada al administrador.',
          variant: 'info'
        });
        this.cdr.detectChanges();
      },
      error: () => {
        this.solicitando = false;
        this.confirm.alert({
          title: 'No se pudo enviar',
          message: 'Ocurrió un error al enviar la solicitud. Inténtalo de nuevo más tarde.',
          variant: 'danger'
        });
        this.cdr.detectChanges();
      }
    });
  }
}
