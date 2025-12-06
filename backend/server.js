import express from "express";
import cors from "cors";
import dotenv from "dotenv";

import authRoutes from "./routes/authRoutes.js";
import productRoutes from "./routes/productRoutes.js";
import userRoutes from "./routes/userRoutes.js";

dotenv.config();

const app = express();

app.use(cors({ origin: "*" }));
app.use(express.json());

// Rutas existentes
app.use("/productos", productRoutes);
app.use("/auth", authRoutes);

// NUEVA ruta de usuarios
app.use("/usuarios", userRoutes);

// Health check
app.get("/health", (_req, res) => res.json({ ok: true }));

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () =>
  console.log(`API lista en http://0.0.0.0:${PORT} (accesible desde la red local)`)
);
