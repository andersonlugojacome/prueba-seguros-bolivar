# React tester rapido para la API

Este folder trae un `App.tsx` simple para consumir la API de polizas en local.

## 1) Levantar backend
En la raiz del proyecto:

```bash
mvn spring-boot:run
```

## 2) Crear app React (Vite) y usar este App.tsx
En otra terminal:

```bash
npm create vite@latest polizas-ui -- --template react-ts
cd polizas-ui
npm install
```

Reemplaza `src/App.tsx` por el archivo:

- `docs/react-test-client/App.tsx`

Luego ejecuta:

```bash
npm run dev
```

## 3) Probar
- Abre la URL de Vite (normalmente `http://localhost:5173`).
- Verifica que `API Key` sea `123456`.
- Prueba por ejemplo:
  - `GET /polizas?tipo=COLECTIVA&estado=VIGENTE`
  - `POST /polizas/3/renovar` (debe dar 400 porque esta cancelada)
  - `GET /polizas` sin API key (debe dar 401)

Nota: el componente ya envia header `x-api-key` por defecto.
