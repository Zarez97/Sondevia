import { Injectable, signal, ApplicationRef, inject } from '@angular/core';

export type ToastVariant = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  message: string;
  variant: ToastVariant;
}

/**
 * Notificaciones flotantes temporales (toasts) con diseño propio.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly appRef = inject(ApplicationRef);
  readonly toasts = signal<Toast[]>([]);
  private counter = 0;

  show(message: string, variant: ToastVariant = 'success', durationMs = 2800): void {
    const id = ++this.counter;
    this.toasts.update(list => [...list, { id, message, variant }]);
    this.programarRender();
    setTimeout(() => this.dismiss(id), durationMs);
  }

  dismiss(id: number): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
    this.programarRender();
  }

  /** Fuerza el render aunque se invoque desde un callback asíncrono (app zoneless). */
  private programarRender(): void {
    queueMicrotask(() => this.appRef.tick());
  }
}
