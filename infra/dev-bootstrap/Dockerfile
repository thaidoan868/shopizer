# Install terraform then apply the main.tf file under the /work/terraform directory
FROM alpine:3.20

ARG TERRAFORM_VERSION=1.7.5

RUN apk add --no-cache bash curl unzip ca-certificates \
  && curl -fsSLo /tmp/terraform.zip https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
  && unzip /tmp/terraform.zip -d /usr/local/bin \
  && rm -f /tmp/terraform.zip

# Optional: MinIO client (mc)
RUN curl -fsSLo /usr/local/bin/mc https://dl.min.io/client/mc/release/linux-amd64/mc \
  && chmod +x /usr/local/bin/mc

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
