import { useState } from "react";

type HttpMethod = "GET" | "POST";

const API_BASE = "http://localhost:8080";
const DEFAULT_API_KEY = "123456";

export default function App() {
  const [apiKey, setApiKey] = useState(DEFAULT_API_KEY);
  const [endpoint, setEndpoint] = useState("/polizas?tipo=COLECTIVA&estado=VIGENTE");
  const [method, setMethod] = useState<HttpMethod>("GET");
  const [body, setBody] = useState('{"descripcion":"Riesgo demo"}');
  const [status, setStatus] = useState<string>("");
  const [response, setResponse] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const runRequest = async () => {
    setLoading(true);
    setStatus("");
    setResponse("");

    try {
      const options: RequestInit = {
        method,
        headers: {
          "x-api-key": apiKey,
          "Content-Type": "application/json",
        },
      };

      if (method !== "GET" && body.trim()) {
        options.body = body;
      }

      const res = await fetch(`${API_BASE}${endpoint}`, options);
      const text = await res.text();

      setStatus(`${res.status} ${res.statusText}`);
      setResponse(text || "(sin contenido)");
    } catch (err) {
      setStatus("Error de red");
      setResponse(String(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main style={{ fontFamily: "sans-serif", maxWidth: 900, margin: "2rem auto", padding: "1rem" }}>
      <h1>Tester API Pólizas</h1>
      <p>Backend esperado en {API_BASE}</p>

      <label style={{ display: "block", marginBottom: 8 }}>
        API Key
        <input
          style={{ width: "100%", padding: 8, marginTop: 4 }}
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
        />
      </label>

      <div style={{ display: "grid", gridTemplateColumns: "120px 1fr", gap: 12, alignItems: "center" }}>
        <select value={method} onChange={(e) => setMethod(e.target.value as HttpMethod)} style={{ padding: 8 }}>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
        </select>

        <input
          style={{ width: "100%", padding: 8 }}
          value={endpoint}
          onChange={(e) => setEndpoint(e.target.value)}
          placeholder="/polizas"
        />
      </div>

      <label style={{ display: "block", marginTop: 12 }}>
        Body JSON (solo para POST)
        <textarea
          style={{ width: "100%", minHeight: 120, padding: 8, marginTop: 4 }}
          value={body}
          onChange={(e) => setBody(e.target.value)}
        />
      </label>

      <button onClick={runRequest} disabled={loading} style={{ marginTop: 12, padding: "10px 16px" }}>
        {loading ? "Enviando..." : "Ejecutar"}
      </button>

      <h2 style={{ marginTop: 20 }}>Respuesta</h2>
      <p><strong>Status:</strong> {status || "-"}</p>
      <pre style={{ background: "#f5f5f5", border: "1px solid #ddd", padding: 12, whiteSpace: "pre-wrap" }}>
        {response || "-"}
      </pre>

      <h3>Endpoints útiles</h3>
      <ul>
        <li>GET /polizas?tipo=COLECTIVA&estado=VIGENTE</li>
        <li>GET /polizas/2/riesgos</li>
        <li>POST /polizas/2/renovar</li>
        <li>POST /polizas/2/cancelar</li>
        <li>POST /polizas/2/riesgos body: {"{"}"descripcion":"Riesgo X"{"}"}</li>
        <li>POST /riesgos/2/cancelar</li>
      </ul>
    </main>
  );
}
