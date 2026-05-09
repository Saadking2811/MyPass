import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

const FLIGHTS = [
  { id: 'f1', ref: 'NM2025A', last: 'NAMOUNE', name: 'Saad Namoune', flight: 'AH 701',  aircraft: 'Airbus A330-200', origin: 'ALG', originCity: 'Algiers', dest: 'CDG', destCity: 'Paris',     dep: '2026-04-15 10:30', arr: '2026-04-15 13:45', dur: '3h 15m', gate: 'A12', term: 'T1', class: 'Economy',  price: 45000  },
  { id: 'f2', ref: 'NM2025B', last: 'NAMOUNE', name: 'Saad Namoune', flight: 'AH 244',  aircraft: 'Boeing 737-800',  origin: 'ALG', originCity: 'Algiers', dest: 'DXB', destCity: 'Dubai',     dep: '2026-04-16 21:10', arr: '2026-04-17 05:05', dur: '6h 55m', gate: 'C03', term: 'T2', class: 'Business', price: 120000 },
  { id: 'f3', ref: 'NM2025C', last: 'NAMOUNE', name: 'Saad Namoune', flight: 'AH 1018', aircraft: 'ATR 72-600',      origin: 'ALG', originCity: 'Algiers', dest: 'ORN', destCity: 'Oran',      dep: '2026-04-17 07:00', arr: '2026-04-17 08:15', dur: '1h 15m', gate: 'B08', term: 'T1', class: 'Economy',  price: 12000  },
  { id: 'f4', ref: 'KL45PQ',  last: 'BENALI',  name: 'Karim Benali',  flight: 'AH 510',  aircraft: 'Boeing 737-800',  origin: 'ALG', originCity: 'Algiers', dest: 'IST', destCity: 'Istanbul',  dep: '2026-04-18 14:30', arr: '2026-04-18 19:15', dur: '3h 45m', gate: 'D05', term: 'T1', class: 'Economy',  price: 55000  },
  { id: 'f5', ref: 'QW34TY',  last: 'MARTINEZ',name: 'Carlos Martinez',flight: 'AH 860',  aircraft: 'Airbus A330-200', origin: 'ALG', originCity: 'Algiers', dest: 'JFK', destCity: 'New York',  dep: '2026-04-19 22:00', arr: '2026-04-20 04:30', dur: '9h 30m', gate: 'A01', term: 'T1', class: 'Business', price: 250000 },
];

async function main() {
  console.log('🌱 Seeding database...');

  // ── User ──
  const passwordHash = await bcrypt.hash('namoune123', 12);
  await prisma.user.upsert({
    where: { email: 'namoune@mypass.dz' },
    create: {
      id: 'user-namoune-001',
      fullName: 'Saad Namoune',
      email: 'namoune@mypass.dz',
      phone: '+213 555 123 456',
      passwordHash,
    },
    update: {},
  });
  console.log('  ✓ User: namoune@mypass.dz / namoune123');

  // ── Flights + seat maps ──
  for (const f of FLIGHTS) {
    const existing = await prisma.flight.findUnique({ where: { id: f.id } });
    if (existing) continue;

    await prisma.flight.create({
      data: {
        id: f.id,
        bookingReference: f.ref,
        lastName: f.last,
        passengerName: f.name,
        flightNumber: f.flight,
        aircraftType: f.aircraft,
        origin: f.origin,
        originCity: f.originCity,
        destination: f.dest,
        destinationCity: f.destCity,
        departureTime: f.dep,
        arrivalTime: f.arr,
        duration: f.dur,
        gate: f.gate,
        terminal: f.term,
        seatClass: f.class,
        price: f.price,
      },
    });

    // Seat map (180 seats per aircraft)
    const offset = f.ref.split('').reduce((a, c) => a + c.charCodeAt(0), 0) % 7;
    const seats: Array<Record<string, unknown>> = [];
    for (let row = 1; row <= 30; row++) {
      const cols = ['A', 'B', 'C', 'D', 'E', 'F'];
      cols.forEach((col, i) => {
        const isPremium = row <= 4;
        const isExtraLeg = row === 12 || row === 13;
        const isWindow = col === 'A' || col === 'F';
        const isAisle = col === 'C' || col === 'D';
        const isOccupied = (row + i + offset) % 5 === 0 || (row * i + offset) % 11 === 0;
        const seatPrice = isPremium ? 5000 : isExtraLeg ? 2500 : isWindow ? 1200 : isAisle ? 800 : 0;
        seats.push({
          flightId: f.id,
          seatCode: `${row}${col}`,
          row,
          column: col,
          premium: isPremium,
          occupied: isOccupied,
          extraLegroom: isExtraLeg,
          window: isWindow,
          aisle: isAisle,
          price: seatPrice,
        });
      });
    }
    await prisma.seat.createMany({ data: seats as never });
  }
  console.log(`  ✓ ${FLIGHTS.length} flights with seat maps`);

  console.log('✅ Seeding complete.');
}

main()
  .then(async () => {
    await prisma.$disconnect();
  })
  .catch(async (e) => {
    console.error(e);
    await prisma.$disconnect();
    process.exit(1);
  });
