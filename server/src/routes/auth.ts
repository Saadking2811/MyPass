import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { randomUUID } from 'node:crypto';
import { prisma } from '../lib/prisma.js';
import { hashPassword, verifyPassword, signToken } from '../lib/auth.js';

const registerSchema = z.object({
  fullName: z.string().min(2).max(200),
  email: z.string().email().max(200),
  phone: z.string().max(50).default(''),
  password: z.string().min(6),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string(),
});

const googleSchema = z.object({
  idToken: z.string(),
  email: z.string().email(),
  displayName: z.string().min(1),
});

const userToDto = (u: {
  id: string; fullName: string; email: string; phone: string;
  avatarUrl: string; createdAt: Date;
}) => ({
  id: u.id,
  fullName: u.fullName,
  email: u.email,
  phone: u.phone,
  avatarUrl: u.avatarUrl,
  createdAt: u.createdAt.toISOString(),
});

export async function registerAuthRoutes(app: FastifyInstance) {
  // ── Register ──
  app.post('/api/auth/register', async (req, reply) => {
    const parsed = registerSchema.safeParse(req.body);
    if (!parsed.success) {
      return reply.code(400).send({
        success: false,
        message: 'Invalid input. Email valid + password min 6 chars.',
      });
    }
    const { fullName, email, phone, password } = parsed.data;
    const cleanEmail = email.toLowerCase().trim();

    const existing = await prisma.user.findUnique({ where: { email: cleanEmail } });
    if (existing) {
      return reply.code(409).send({
        success: false,
        message: 'Email already registered.',
      });
    }

    const user = await prisma.user.create({
      data: {
        fullName: fullName.trim(),
        email: cleanEmail,
        phone: phone.trim(),
        passwordHash: await hashPassword(password),
      },
    });

    return {
      success: true,
      user: userToDto(user),
      token: signToken(user.id),
      message: 'Registration successful',
    };
  });

  // ── Login ──
  app.post('/api/auth/login', async (req, reply) => {
    const parsed = loginSchema.safeParse(req.body);
    if (!parsed.success) {
      return reply.code(400).send({ success: false, message: 'Invalid input' });
    }
    const { email, password } = parsed.data;

    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase().trim() },
    });
    if (!user || !(await verifyPassword(password, user.passwordHash))) {
      return reply.code(401).send({ success: false, message: 'Invalid credentials.' });
    }

    return {
      success: true,
      user: userToDto(user),
      token: signToken(user.id),
      message: 'Login successful',
    };
  });

  // ── Google sign-in (creates user if missing) ──
  app.post('/api/auth/google', async (req, reply) => {
    const parsed = googleSchema.safeParse(req.body);
    if (!parsed.success) {
      return reply.code(400).send({ success: false, message: 'Invalid input' });
    }
    const { email, displayName } = parsed.data;
    const cleanEmail = email.toLowerCase().trim();

    let user = await prisma.user.findUnique({ where: { email: cleanEmail } });
    if (!user) {
      user = await prisma.user.create({
        data: {
          fullName: displayName.trim(),
          email: cleanEmail,
          passwordHash: await hashPassword(randomUUID()),
        },
      });
    }

    return {
      success: true,
      user: userToDto(user),
      token: signToken(user.id),
      message: 'Google sign-in successful',
    };
  });
}
