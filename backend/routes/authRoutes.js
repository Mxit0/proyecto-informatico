import { Router } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { findUserByEmail, createUser } from '../repositories/userRepository.js';

const router = Router();

// REGISTER
router.post('/register', async (req, res) => {
  try {
    const { nombre_usuario, correo, password } = req.body;

    const exists = await findUserByEmail(correo);
    if (exists) return res.status(409).json({ ok: false, message: "Correo ya registrado" });

    const passwordHash = await bcrypt.hash(password, 10);
    const user = await createUser({ nombre_usuario, correo, passwordHash });

    res.status(201).json({ ok: true, user });
  } catch (err) {
    console.error(err);
    res.status(500).json({ ok: false, message: "Error interno" });
  }
});

// LOGIN
router.post('/login', async (req, res) => {
  try {
    const { correo, password } = req.body;

    const user = await findUserByEmail(correo);
    if (!user) return res.status(401).json({ ok: false, message: "Credenciales inválidas" });

    const ok = await bcrypt.compare(password, user.password);
    if (!ok) return res.status(401).json({ ok: false, message: "Credenciales inválidas" });

    delete user.password;

    const token = jwt.sign(user, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.json({ ok: true, user, token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ ok: false, message: "Error interno" });
  }
});

export default router;
