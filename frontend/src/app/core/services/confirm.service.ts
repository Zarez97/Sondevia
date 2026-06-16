import { Injectable, signal, ApplicationRef, inject } from '@angular/core';

export type ConfirmVariant = 'danger' | 'warning' | 'primary' | 'info';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: ConfirmVariant;
}

export interface ConfirmState extends ConfirmOptions {
  confirmText: string;
  cancelText: string;
  variant: ConfirmVariant;
  showCancel: boolean;
}

/**
 * Diálogo de confirmación / aviso global con diseño propio.
 * Reemplaza los confirm()/alert() nativos del navegador.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly appRef = inject(ApplicationRef);
  readonly state = signal<ConfirmState | null>(null);
  private resolver?: (v: boolean) => void;

  /** Diálogo de confirmación (dos botones). Resuelve true/false. */
  ask(opts: ConfirmOptions): Promise<boolean> {
    this.state.set({
      ...opts,
      confirmText: opts.confirmText ?? 'Confirmar',
      cancelText: opts.cancelText ?? 'Cancelar',
      variant: opts.variant ?? 'primary',
      showCancel: true,
    });
    this.programarRender();
    return new Promise<boolean>(res => (this.resolver = res));
  }

  /** Aviso simple (un solo botón). */
  alert(opts: ConfirmOptions): Promise<boolean> {
    this.state.set({
      ...opts,
      confirmText: opts.confirmText ?? 'Entendido',
      cancelText: '',
      variant: opts.variant ?? 'info',
      showCancel: false,
    });
    this.programarRender();
    return new Promise<boolean>(res => (this.resolver = res));
  }

  resolve(value: boolean): void {
    this.state.set(null);
    const r = this.resolver;
    this.resolver = undefined;
    this.programarRender();
    r?.(value);
  }

  /**
   * Fuerza el render del diálogo aunque se invoque desde un callback asíncrono
   * (la app es zoneless; un click dispara render solo, pero un callback HTTP no).
   */
  private programarRender(): void {
    queueMicrotask(() => this.appRef.tick());
  }
}
