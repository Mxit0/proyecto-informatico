import jwt from 'jsonwebtoken';

function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'No autorizado' });

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded; // { id_usuario, correo, ... }
    next();
  } catch {
    return res.status(401).json({ error: 'Token inválido o expirado' });
  }
}

export default requireAuth;