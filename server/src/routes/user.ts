import { FastifyInstance } from 'fastify';
import { prisma } from '../lib/prisma.js';

export async function registerUserRoutes(app: FastifyInstance) {
  // ── All boarding passes for a user ──
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
      // Return as plain array (matches Android expectation)
      return passes.map((p) => ({ ...p, issuedAt: p.issuedAt.toISOString() }));
    }
  );

  // ── Single user profile ──
  app.get<{ Params: { userId: string } }>(
    '/api/user/:userId',
    async (req, reply) => {
      const u = await prisma.user.findUnique({ where: { id: req.params.userId } });
      if (!u) return reply.code(404).send({ success: false, message: 'User not found' });
      return {
        id: u.id,
        fullName: u.fullName,
        email: u.email,
        phone: u.phone,
        avatarUrl: u.avatarUrl,
        createdAt: u.createdAt.toISOString(),
      };
    }
  );
}
