.PHONY:
up:
	docker ps > /dev/null || systemctl --user start docker-desktop
	docker ps > /dev/null || sleep 4
	docker compose up -d
down:
	docker compose down
restart:
	docker compose down
	docker compose up -d
reset: # delete all volumes
	docker compose down -v
	docker compose up -d
rebuild:
	docker compose up -d --build
