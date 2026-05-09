import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

const JWT_SECRET = process.env.JWT_SECRET ?? 'dev-secret-change-me';
const JWT_EXPIRES_IN = '30d';

export const hashPassword = (password: string) => bcrypt.hash(password, 12);

export const verifyPassword = (password: string, hash: string) =>
  bcrypt.compare(password, hash);

export const signToken = (userId: string) =>
  jwt.sign({ userId }, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });

export const verifyToken = (token: string): { userId: string } | null => {
  try {
    const decoded = jwt.verify(token, JWT_SECRET) as { userId: string };
    return decoded;
  } catch {
    return null;
  }
};

/** Extract userId from JWT (Authorization header) or from X-User-Id header. */
export const resolveUserId = (
  authHeader: string | undefined,
  userIdHeader: string | string[] | undefined,
): string | null => {
  if (authHeader?.startsWith('Bearer ')) {
    const token = authHeader.slice('Bearer '.length).trim();
    const decoded = verifyToken(token);
    if (decoded?.userId) return decoded.userId;
  }
  if (typeof userIdHeader === 'string' && userIdHeader.trim()) {
    return userIdHeader.trim();
  }
  return null;
};
