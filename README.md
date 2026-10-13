# http4s-ts-app

Scala 3 + http4s (Ember) + cats-effect backend serving a small TypeScript frontend.

```
build.sbt                  sbt build (Scala 3, http4s 0.23, cats-effect 3, circe)
src/main/scala/app/
  Main.scala               IOApp: wires routes and starts Ember on :8080
  Routes.scala             JSON API under /api + static file service
  TodoRepo.scala           in-memory store using cats.effect.Ref
  Todo.scala               models with derived circe codecs
frontend/                  TypeScript sources (compiled by tsc into static/js)
static/index.html          page served at /
```

## Run

```bash
cd frontend && npm install && npm run build
```

```bash
sbt run            # or: PORT=9090 sbt run
```

Open http://localhost:8080. Use `npm run watch` in `frontend/` while editing TS.

## API

| Method | Path                      | Description        |
|--------|---------------------------|--------------------|
| GET    | /api/hello?name=X         | greeting           |
| GET    | /api/todos                | list todos         |
| POST   | /api/todos `{"title":..}` | create todo        |
| POST   | /api/todos/{id}/toggle    | toggle done        |
| DELETE | /api/todos/{id}           | delete todo        |
