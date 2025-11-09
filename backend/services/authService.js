import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { findUserByEmail, createUser } from '../repositories/userRepository.js';

const TOKEN_TTL = '7d'; // ajusta a tu gusto

export async function registerUser({ nombre_usuario, correo, password, foto }) {
  const existing = await findUserByEmail(correo);
  if (existing) {
    const err = new Error('El correo ya está registrado');
    err.status = 409;
    throw err;
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const user = await createUser({ nombre_usuario, correo, passwordHash, foto });
  const token = signJwt(user);
  return { user, token };
}

export async function loginUser({ correo, password }) {
  const user = await findUserByEmail(correo);
  if (!user) {
    const err = new Error('Credenciales inválidas');
    err.status = 401;
    throw err;
  }

  const ok = await bcrypt.compare(password, user.password || '');
  if (!ok) {
    const err = new Error('Credenciales inválidas');
    err.status = 401;
    throw err;
  }

  // Limpia campos sensibles antes de firmar
  const safeUser = {
    id_usuario: user.id_usuario,
    nombre_usuario: user.nombre_usuario,
    correo: user.correo,
    foto: user.foto ?? null
  };

  const token = signJwt(safeUser);
  return { user: safeUser, token };
}

function signJwt(payload) {
  return jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: TOKEN_TTL });
}
