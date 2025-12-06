import bcrypt from 'bcryptjs';
import jwt from "jsonwebtoken";
import { pool } from "../db.js";

export async function login(req, res) {
  const { correo, password } = req.body;

  const result = await pool.query(
    "SELECT id_usuario, nombre_usuario, correo, password FROM usuario WHERE correo = $1",
    [correo]
  );

  if (result.rows.length === 0)
    return res.status(400).json({ error: "Usuario no encontrado" });

  const user = result.rows[0];

  const match = await bcrypt.compare(password, user.password);
  if (!match) return res.status(401).json({ error: "Contraseña incorrecta" });

  const token = jwt.sign(
    { id: user.id_usuario, correo: user.correo },
    process.env.JWT_SECRET,
    { expiresIn: "2h" }
  );

  res.json({
    token,
    usuario: {
      id: user.id_usuario,
      nombre: user.nombre_usuario,
      correo: user.correo,
    },
  });
}
