import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

import authRoutes from './routes/authRoutes.js';
import productRoutes from './routes/productRoutes.js';
import userRoutes from './routes/userRoutes.js';

dotenv.config();

const app = express();

app.use(cors({ origin: "*" }));
app.use(express.json());

// Rutas existentes
app.use('/productos', productRoutes);
app.use('/auth', authRoutes);

// NUEVA ruta de usuarios
app.use('/usuarios', userRoutes);

// Health check
app.get('/health', (_req, res) => res.json({ ok: true }));

app.listen(process.env.PORT || 3000, () =>
  console.log(`API lista en http://localhost:${process.env.PORT}`)
);
