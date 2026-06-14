import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.css'
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  errorMessage = '';
  showPassword = false;
  redirectUrl: string | null = null;
  readonly hoy = new Date().toISOString().split('T')[0];

  togglePassword(): void { this.showPassword = !this.showPassword; }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {
    this.redirectUrl = this.route.snapshot.queryParamMap.get('redirect');
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(128)]],
      email: ['', [Validators.required, Validators.email]],
      contrasenia: ['', [Validators.required, Validators.minLength(8)]],
      confirmar: ['', Validators.required],
      fechaNacimiento: ['', Validators.required]
    }, { validators: RegisterComponent.passwordsIguales });
  }

  static passwordsIguales(group: AbstractControl): ValidationErrors | null {
    const pass = group.get('contrasenia')?.value;
    const conf = group.get('confirmar')?.value;
    return pass && conf && pass !== conf ? { noCoincide: true } : null;
  }

  get f() { return this.form.controls; }
  get loginQueryParams() {
    return this.redirectUrl ? { redirect: this.redirectUrl } : {};
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;

    this.loading = true;
    this.errorMessage = '';

    const { nombre, email, contrasenia, fechaNacimiento } = this.form.value;
    this.authService.registrar({ nombre, email, contrasenia, fechaNacimiento }).subscribe({
      next: () => this.router.navigateByUrl(this.redirectUrl || '/dashboard'),
      error: (e) => {
        this.errorMessage = e.error?.mensaje || 'No se pudo completar el registro.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
