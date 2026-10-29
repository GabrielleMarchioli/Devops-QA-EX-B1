   # Jenkins do laboratorio B1: Java 21 (Core), Python (servicos) e cliente Docker (imagens)
   FROM docker:27-cli AS dockercli

   FROM jenkins/jenkins:lts-jdk21
   USER root
   RUN apt-get update \
       && apt-get install -y --no-install-recommends python3 python3-venv python3-pip curl \
       && rm -rf /var/lib/apt/lists/*
   # cliente Docker + plugins (buildx e compose) copiados da imagem oficial
   COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
   COPY --from=dockercli /usr/local/libexec/docker/cli-plugins /usr/local/libexec/docker/cli-plugins