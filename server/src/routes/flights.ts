import { FastifyInstance } from 'fastify';
import { prisma } from '../lib/prisma.js';

export async function registerFlightRoutes(app: FastifyInstance) {
  // ── Flight lookup by booking ref + last name ──
  app.get<{ Querystring: { bookingRef?: string; lastName?: string } }>(
    '/api/flights/lookup',
    async (req, reply) => {
      const bookingRef = req.query.bookingRef?.trim().toUpperCase();
      const lastName = req.query.lastName?.trim().toUpperCase();

      if (!bookingRef || !lastName) {
        return reply.code(400).send({
          success: false,
          message: 'bookingRef and lastName are required',
        });
      }

      const flight = await prisma.flight.findFirst({
        where: {
          bookingReference: bookingRef,
          lastName: { equals: lastName, mode: 'insensitive' },
        },
      });

      if (!flight) {
        return reply.code(404).send({
          success: false,
          message: `No booking found for ${bookingRef} / ${lastName}`,
        });
      }

      return { success: true, flight };
    }
  );

  // ── Seat map for a flight ──
  app.get<{ Params: { flightId: string } }>(
    '/api/flights/:flightId/seats',
    async (req) => {
      const seats = await prisma.seat.findMany({
        where: { flightId: req.params.flightId },
        orderBy: [{ row: 'asc' }, { column: 'asc' }],
      });
      return { success: true, seats };
    }
  );
}
