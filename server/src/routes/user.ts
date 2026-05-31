import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { resolveUserId } from '../lib/auth.js';

const profileUpdateSchema = z.object({
  fullName: z.string().min(2).max(200).optional(),
  phone: z.string().max(50).optional(),
  avatarUrl: z.string().max(500).optional(),
});

export async function registerUserRoutes(app: FastifyInstance) {
  // ── User profile ──
  app.get<{ Params: { userId: string } }>(
    '/api/user/:userId',
    async (req, reply) => {
      const u = await prisma.user.findUnique({ where: { id: req.params.userId } });
      if (!u) return reply.code(404).send({ success: false, message: 'User not found' });
      return {
        success: true,
        user: {
          id: u.id,
          fullName: u.fullName,
          email: u.email,
          phone: u.phone,
          avatarUrl: u.avatarUrl,
          createdAt: u.createdAt.toISOString(),
        },
      };
    }
  );

  // ── Update user profile (full name, phone, avatar) ──
  app.patch<{ Params: { userId: string } }>(
    '/api/user/:userId',
    async (req, reply) => {
      const authedId = resolveUserId(
        req.headers.authorization,
        req.headers['x-user-id'] as string | undefined
      );
      if (!authedId || authedId !== req.params.userId) {
        return reply.code(403).send({ success: false, message: 'Forbidden' });
      }
      const parsed = profileUpdateSchema.safeParse(req.body);
      if (!parsed.success) {
        return reply.code(400).send({ success: false, message: 'Invalid input', errors: parsed.error.issues });
      }
      const data = parsed.data;
      const u = await prisma.user.update({
        where: { id: req.params.userId },
        data: {
          ...(data.fullName !== undefined && { fullName: data.fullName.trim() }),
          ...(data.phone !== undefined && { phone: data.phone.trim() }),
          ...(data.avatarUrl !== undefined && { avatarUrl: data.avatarUrl.trim() }),
        },
      });
      return {
        success: true,
        user: {
          id: u.id,
          fullName: u.fullName,
          email: u.email,
          phone: u.phone,
          avatarUrl: u.avatarUrl,
          createdAt: u.createdAt.toISOString(),
        },
      };
    }
  );

  // ── All boarding passes for a user (active + archived) ──
  app.get<{ Params: { userId: string } }>(
    '/api/user/:userId/boardingpasses',
    async (req, reply) => {
      const userId = req.params.userId;
      if (!userId) {
        return reply.code(400).send({ success: false, message: 'userId required' });
      }
      const passes = await prisma.boardingPass.findMany({
        where: { userId },
        orderBy: { issuedAt: 'desc' },
      });
      return passes.map((p) => ({ ...p, issuedAt: p.issuedAt.toISOString() }));
    }
  );

  // ── All flights linked to a user (via check-ins) ──
  app.get<{ Params: { userId: string } }>(
    '/api/user/:userId/flights',
    async (req) => {
      const userId = req.params.userId;

      // Flights via check-ins
      const checkIns = await prisma.checkIn.findMany({
        where: { userId },
        select: { flight: true },
        distinct: ['flightId'],
      });
      const userFlights = checkIns.map((ci) => ci.flight);

      // Also include flights matching the user's last name (so upcoming bookings show up)
      const user = await prisma.user.findUnique({ where: { id: userId } });
      const lastNameUpper = user?.fullName.split(' ').pop()?.toUpperCase() ?? '';
      const lastNameFlights = lastNameUpper
        ? await prisma.flight.findMany({
            where: { lastName: { equals: lastNameUpper, mode: 'insensitive' } },
          })
        : [];

      // De-duplicate by id
      const byId = new Map<string, typeof userFlights[number]>();
      [...userFlights, ...lastNameFlights].forEach((f) => byId.set(f.id, f));
      return Array.from(byId.values());
    }
  );
}
