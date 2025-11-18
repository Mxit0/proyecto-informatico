import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

import authRoutes from './routes/authRoutes.js';
import productRoutes from './routes/productRoutes.js';
import userRoutes from './routes/userRoutes.js';

import compatibilityTestRoutes from './routes/compatibilityTestRoutes.js';

import compatibilityRoutes from './routes/compatibilityRoutes.js';

dotenv.config();

const app = express();

app.use(cors({ origin: "*" }));
app.use(express.json());

// Rutas existentes
app.use('/productos', productRoutes);
app.use('/auth', authRoutes);
app.use('/api/compatibility', compatibilityRoutes);
app.use('/api/compatibility-test', compatibilityTestRoutes);

// NUEVA ruta de usuarios
app.use('/usuarios', userRoutes);

// Health check
app.get('/health', (_req, res) => res.json({ ok: true }));

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
  console.log(`API lista en http://localhost:${PORT}`);
});