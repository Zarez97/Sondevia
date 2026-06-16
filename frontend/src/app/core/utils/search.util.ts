/**
 * Utilidades para búsqueda dinámica (filtrado en cliente).
 * Normaliza a minúsculas y elimina tildes para comparar de forma flexible.
 */
export function normalizar(valor: unknown): string {
  return (valor ?? '')
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '');
}

/** True si el término aparece en alguno de los campos (o si el término está vacío). */
export function coincide(termino: string, ...campos: unknown[]): boolean {
  const t = normalizar(termino).trim();
  if (!t) return true;
  return campos.some(c => normalizar(c).includes(t));
}
