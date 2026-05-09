import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { resolveUserId } from '../lib/auth.js';

const passportSchema = z.object({
  fullName: z.string().default(''),
  passportNumber: z.string().default(''),
  nationality: z.string().default(''),
  dateOfBirth: z.string().default(''),
  expiryDate: z.string().default(''),
  gender: z.string().default(''),
});

const baggageSchema = z.object({
  checkedBags: z.number().int().min(0).default(0),
  carryOnBags: z.number().int().min(0).default(1),
  oversizedBags: z.number().int().min(0).default(0),
});

const requestsSchema = z.object({
  dietaryPreference: z.string().default('Standard'),
  needsAssistance: z.boolean().default(false),
  travelingWithInfant: z.boolean().default(false),
  travelingWithPet: z.boolean().default(false),
  notes: z.string().default(''),
});

const checkInSchema = z.object({
  userId: z.string().default(''),
  bookingReference: z.string().min(1),
  passengerName: z.string().min(1),
  passportInfo: passportSchema.default({}),
  selectedSeat: z.string().min(1),
  baggage: baggageSchema.default({}),
  specialRequests: requestsSchema.default({}),
});

export async function registerCheckInRoutes(app: FastifyInstance) {
  // ── Complete check-in ──
  app.post('/api/checkin/complete', async (req, reply) => {
    const parsed = checkInSchema.safeParse(req.body);
    if (!parsed.success) {
      return reply.code(400).send({ success: false, message: 'Invalid input', errors: parsed.error.issues });
    }
    const data = parsed.data;

    const flight = await prisma.flight.findFirst({
      where: { bookingReference: data.bookingReference.toUpperCase() },
    });
    if (!flight) {
      return reply.code(404).send({ success: false, message: 'Flight not found' });
    }

    // Resolve userId (body, JWT, or X-User-Id header)
    const headerUserId = resolveUserId(
      req.headers.authorization,
      req.headers['x-user-id'] as string | undefined
    );
    const candidateUserId = data.userId.trim() || headerUserId || null;
    const userId = candidateUserId
      ? (await prisma.user.findUnique({ where: { id: candidateUserId } }))?.id ?? null
      : null;

    const seat = data.selectedSeat;
    const baggageStr =
      [
        data.baggage.checkedBags > 0 ? `${data.baggage.checkedBags} checked` : '',
        data.baggage.carryOnBags > 0 ? `${data.baggage.carryOnBags} carry-on` : '',
        data.baggage.oversizedBags > 0 ? `${data.baggage.oversizedBags} oversized` : '',
      ].filter(Boolean).join(' ').trim() || 'No baggage';

    const qrPayload = [
      'M1',
      data.passengerName.slice(0, 20).toUpperCase().replace(/ /g, '/'),
      `E${data.bookingReference.toUpperCase()}`,
      `${flight.origin}${flight.destination}`,
      flight.flightNumber,
      '127',
      `${seat.padStart(4, '0')}001`,
    ].join('');

    const firstSeatRow = parseInt(seat.match(/^(\d+)/)?.[1] ?? '99', 10);
    const boardingGroup = firstSeatRow <= 4 ? 'Priority' : 'A';
    const sequence = String(Math.floor(Math.random() * 200) + 1).padStart(3, '0');

    // Atomic: mark seat occupied + create BP + create CheckIn + update flight
    const result = await prisma.$transaction(async (tx) => {
      await tx.seat.updateMany({
        where: { flightId: flight.id, seatCode: seat },
        data: { occupied: true },
      });
      await tx.flight.update({
        where: { id: flight.id },
        data: { checkInStatus: 'Checked-In' },
      });

      const bp = await tx.boardingPass.create({
        data: {
          userId,
          bookingReference: data.bookingReference.toUpperCase(),
          passengerName: data.passengerName,
          flightNumber: flight.flightNumber,
          airlineName: flight.airlineName,
          origin: flight.origin,
          originCity: flight.originCity,
          destination: flight.destination,
          destinationCity: flight.destinationCity,
          departureTime: flight.departureTime,
          arrivalTime: flight.arrivalTime,
          gate: flight.gate,
          terminal: flight.terminal,
          seat,
          seatClass: flight.seatClass,
          boardingGroup,
          sequence,
          qrPayload,
          baggageInfo: baggageStr,
          passportNumber: data.passportInfo.passportNumber,
          passportName: data.passportInfo.fullName,
          nationality: data.passportInfo.nationality,
        },
      });

      await tx.checkIn.create({
        data: {
          userId,
          flightId: flight.id,
          boardingPassId: bp.id,
          passportNumber: data.passportInfo.passportNumber,
          passportName: data.passportInfo.fullName,
          passportNationality: data.passportInfo.nationality,
          passportDob: data.passportInfo.dateOfBirth,
          passportExpiry: data.passportInfo.expiryDate,
          passportGender: data.passportInfo.gender,
          selectedSeat: seat,
          checkedBags: data.baggage.checkedBags,
          carryOnBags: data.baggage.carryOnBags,
          oversizedBags: data.baggage.oversizedBags,
          dietaryPreference: data.specialRequests.dietaryPreference,
          needsAssistance: data.specialRequests.needsAssistance,
          travelingWithInfant: data.specialRequests.travelingWithInfant,
          travelingWithPet: data.specialRequests.travelingWithPet,
          notes: data.specialRequests.notes,
        },
      });

      return bp;
    });

    return {
      success: true,
      boardingPass: { ...result, issuedAt: result.issuedAt.toISOString() },
      message: 'Check-in completed successfully',
    };
  });

  // ── Get a single boarding pass ──
  app.get<{ Params: { passId: string } }>(
    '/api/checkin/:passId',
    async (req, reply) => {
      const bp = await prisma.boardingPass.findUnique({
        where: { id: req.params.passId },
      });
      if (!bp) {
        return reply.code(404).send({ success: false, message: 'Boarding pass not found' });
      }
      return {
        success: true,
        boardingPass: { ...bp, issuedAt: bp.issuedAt.toISOString() },
      };
    }
  );
}
