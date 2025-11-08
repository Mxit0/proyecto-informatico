// backend/server.js
const express = require('express');
const dotenv = require('dotenv');
const { createClient } = require('@supabase/supabase-js');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Supabase admin (SERVICE ROLE SOLO EN BACKEND)
const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
);

app.use(express.json());

// Salud
app.get('/', (_req, res) => {
  res.status(200).json({ status: 'Online', message: 'API OK' });
});

// Endpoint simple: verificar conexión consultando 1
app.get('/api/v1/db-status', async (_req, res) => {
  const { error } = await supabase.from('usuario').select('id_usuario').limit(1);
  if (error) return res.status(500).json({ status: 'Error', error: error.message });
  res.json({ status: 'Connected', database: 'Supabase' });
});

// (Opcional) Login en backend, usando email/password de Supabase Auth
app.post('/api/v1/login', async (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) return res.status(400).json({ error: 'Faltan credenciales' });

  const { data, error } = await supabase.auth.signInWithPassword({ email, password });
  if (error) return res.status(401).json({ error: error.message });

  res.json({ session: data.session, user: data.user });
});

app.listen(PORT, () => {
  console.log(`API en http://localhost:${PORT}`);
});
