import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export interface MenuItem {
  nombre: string;
  url: string;
}

@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly API = `${environment.apiUrl}/auth/menu`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  obtenerMenu(): Observable<MenuItem[]> {
    const headers = new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
    return this.http.get<MenuItem[]>(this.API, { headers });
  }
}
