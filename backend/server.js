import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import authRoutes from  './routes/authRoutes.js';;
import productRoutes from './routes/productRoutes.js';

dotenv.config();

const app = express();
app.use(cors({ origin: "*" }));
app.use(express.json());
app.use('/productos', productRoutes);

app.use('/auth', authRoutes);

app.get('/health', (_req, res) => res.json({ ok: true }));

app.listen(process.env.PORT || 3000, () =>
  console.log(`API lista en http://localhost:${process.env.PORT}`)
);
