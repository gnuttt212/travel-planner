# Travel Planner - Backend (Spring Boot)

Nen tang khoi dau cho ung dung len ke hoach du lich, thiet ke theo kien
truc **Modular Monolith**: mot ung dung duy nhat nhung chia thanh cac
module nghiep vu co ranh gioi ro rang, de bao tri o giai doan dau va co
the tach thanh microservices sau nay neu can.

## Cau truc thu muc

```
com.travelplanner
├── common/              # ha tang dung chung: config, exception, response, base entity
├── itinerary/            # module Lich trinh + goi y diem den (AI) - da implement mau
│   ├── domain/           # entity
│   ├── dto/              # request/response
│   ├── repository/       # Spring Data JPA
│   ├── service/           # interface (Controller phu thuoc vao day)
│   │   └── impl/          # implementation
│   ├── mapper/            # chuyen doi entity <-> dto
│   └── controller/       # REST endpoint
├── budget/               # module Ngan sach (package rong, xem package-info.java)
├── collaboration/        # module Cong tac nhom - realtime qua WebSocket (package rong)
└── booking/              # module Dat dich vu - Adapter pattern goi ben thu 3 (package rong)
```

Moi module tiep theo (budget, collaboration, booking) nen theo dung cau
truc con nhu itinerary: `domain / dto / repository / service / service.impl
/ mapper / controller`. Xem file `package-info.java` trong tung package
de biet dinh huong entity va endpoint goi y.

## Chay du an

1. Khoi dong PostgreSQL + Redis bang Docker:
   ```bash
   docker compose up -d
   ```

2. Chay ung dung:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Truy cap Swagger UI de xem/test API:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## Vi du goi API module Itinerary

```bash
# Tao diem den
curl -X POST http://localhost:8080/api/v1/destinations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vung Tau",
    "country": "Vietnam",
    "description": "Bai bien gan thanh pho, hai san tuoi, phu hop di ngan ngay",
    "tags": ["bien", "am-thuc"],
    "bestMonths": [3,4,5,6,7,8],
    "avgCostPerDay": 800000
  }'

# Goi y diem den theo ngan sach va thang di
curl "http://localhost:8080/api/v1/destinations/recommend?maxBudgetPerDay=1000000&travelMonth=8"
```

## Buoc tiep theo de hoan thien du an

1. Trien khai Auth module (JWT), cap nhat lai `SecurityConfig`
2. Trien khai module Budget theo pattern cua Itinerary
3. Trien khai module Collaboration voi WebSocket + STOMP
4. Trien khai module Booking voi Adapter Pattern (FakeBookingProvider)
5. Nang cap co che goi y diem den: them buoc vector search bang
   `pgvector` (embedding mo ta diem den) truoc buoc loc cung hien tai
6. Viet unit test cho service layer (JUnit + Mockito)
7. Them Flyway/Liquibase de quan ly migration thay vi `ddl-auto: update`
