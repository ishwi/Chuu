FROM postgres:14
ARG MBID_TOKEN
ARG POSTGRES_PASSWORD



# Update the Ubuntu and PostgreSQL repository indexes
RUN apt-get update && \
    apt-get install --allow-downgrades --no-install-recommends -qy \
		build-essential \
		ca-certificates \
		wget \
		python3 \
		vim \
		cron \
		less \
		python3-psycopg2 \
		python3-six \
		python3-pip \
		python3-certifi \
		python3-setuptools \
		postgresql-client \
		curl \
		gcc \
		git \
		libicu-dev \
		make \
		pkg-config \
    && apt-get install --allow-downgrades --no-install-recommends -qy \
        --target-release "n=$(. /etc/os-release && echo "$VERSION_CODENAME")-pgdg" \
        libpq5 \
        libpq-dev \
        postgresql-server-dev-14 \
    && rm -rf /var/lib/apt/lists/*

COPY *.sh /usr/local/bin/

RUN pip install -U mbdata
RUN curl https://raw.githubusercontent.com/lalinsky/mbdata/main/mbslave.conf.default -o mbslave.conf
RUN curl https://raw.githubusercontent.com/lalinsky/mbdata/main/mbdata/sql/CreateCollations.sql -o CreateCollations.sql
RUN mv CreateCollations.sql /usr/local/lib/python3.9/dist-packages/mbdata/sql/

RUN mkdir /media/dbdump
RUN mkdir /media/dbdump/LATEST
run chmod -R 777 /media/

RUN mv mbslave.conf  /etc/mbslave.conf && \
     sed  -i -e  "/#password=/ s/.*/password=$POSTGRES_PASSWORD/" /etc/mbslave.conf &&  \
     sed  -i -e  "/#token=/ s/.*/token=$MBID_TOKEN/" /etc/mbslave.conf && \
     sed  -i -e  '/host=.*/ s/.*/host=\/var\/run\/postgresql/' /etc/mbslave.conf
# There is no tag v0.4.2 (or 0.5.0) yet
ARG PG_AMQP_GIT_REF="240d477d40c5e7a579b931c98eb29cef4edda164"
# hadolint ignore=DL3003
RUN git clone https://github.com/omniti-labs/pg_amqp.git /tmp/pg_amqp \
    && cd /tmp/pg_amqp \
    && git checkout "$PG_AMQP_GIT_REF" \
    && make \
    && make install \
    && rm -R /tmp/*

RUN touch /var/log/mbslave.log
RUN echo '45 * * * * mbslave sync >> /var/log/mbslave.log' >  /etc/cron.d/mbslave-sync
RUN chmod 0744 /etc/cron.d/mbslave-sync

#ADD create-db.sh /docker-entrypoint-initdb.d/

CMD ["postgres"]




