import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';

const syncSchema = z.object({
  userId: z.string().min(1),
  lastSyncTimestamp: z.string().default(''),
});

export async function registerSyncRoutes(app: FastifyInstance) {
  app.post('/api/sync', async (req, reply) => {
    const parsed = syncSchema.safeParse(req.body);
    if (!parsed.success) {
      return reply.code(400).send({ success: false, message: 'userId is required' });
    }
    const { userId } = parsed.data;

    const passes = await prisma.boardingPass.findMany({
      where: { userId },
      orderBy: { issuedAt: 'desc' },
    });

    const user = await prisma.user.findUnique({ where: { id: userId } });
    const lastNameUpper = user?.fullName.split(' ').pop()?.toUpperCase() ?? '';
    const flights = lastNameUpper
      ? await prisma.flight.findMany({
          where: { lastName: { equals: lastNameUpper, mode: 'insensitive' } },
        })
      : [];

    return {
      success: true,
      boardingPasses: passes.map((p) => ({ ...p, issuedAt: p.issuedAt.toISOString() })),
      flights,
      timestamp: new Date().toISOString(),
    };
  });
}
