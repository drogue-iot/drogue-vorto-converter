# Drogue IoT Vorto Converter

[![CI](https://github.com/drogue-iot/drogue-vorto-converter/workflows/CI/badge.svg)](https://github.com/drogue-iot/drogue-vorto-converter/actions?query=workflow%3A%22CI%22)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/drogue-iot/drogue-vorto-converter?sort=semver)](https://github.com/drogue-iot/drogue-vorto-converter/releases)
[![Matrix](https://img.shields.io/matrix/drogue-iot:matrix.org)](https://matrix.to/#/#drogue-iot:matrix.org)

This is a [Knative](https://knative.dev/) event converter for IoT related payload, using the
[Eclipse Vorto™](https://www.eclipse.org/vorto/) model transformation.

> Knative: Kubernetes-based platform to deploy and manage modern serverless workloads.

> Vorto provides a simple language to describe IoT device capabilities. The Vorto Repository is an open platform
> to share and manage device descriptions.

## About

This application transforms the payload of [Cloud Events](https://cloudevents.io/) using a Vorto model transformation.

The idea is to run this as part of e.g. a Knative [*Flow*](https://knative.dev/docs/eventing/flows/), and translate
device specific payload into the [Eclipse Ditto™](https://www.eclipse.org/ditto/) digital twin format, based on a
model description and mapping information, stored in a Vorto repository.

## Using

You can see a more integrated example in the [drogue-cloud](https://github.com/drogue-iot/drogue-cloud) repository, as
part of the "digital twin" deployment.

In order to create a Knative service from this image, you can use the following service declaration:

~~~yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: vorto-converter
spec:
  template:
    metadata:
      annotations:
        autoscaling.knative.dev/scaleToZeroPodRetentionPeriod: "5m"
    spec:
      containers:
        - image: ghcr.io/drogue-iot/vorto-converter:latest
~~~

Then, reference this as part of a sequence, e.g. pulling events from Kafka, then converting data before handing it
off to Ditto:

~~~yaml
apiVersion: flows.knative.dev/v1
kind: Sequence
metadata:
  name: digital-twin
spec:
  channelTemplate:
    apiVersion: messaging.knative.dev/v1alpha1
    kind: KafkaChannel
    spec:
      numPartitions: 1
      replicationFactor: 3
  steps:
    - ref:
        apiVersion: serving.knative.dev/v1
        kind: Service
        name: vorto-converter
    - ref:
        kind: Service
        apiVersion: serving.knative.dev/v1
        name: ditto-pusher
~~~

## Public and private models

The Vorto repository allows to create public and private models. This converter can work with both. However, when
private models are required, you need to configure an API key for the Vorto API, which has access to the required
models and their transformations.

If you only work with public models, an API key is not required.

When using [vorto.eclipse.org](https://vorto.eclipse.org), the source of the API key depends on the identity provider
you used for creating your Vorto account. For example, when you logged in to the Vorto repository using your
GitHub account, then you need to create a GitHub personal access token, with the following permissions:

* `read:user`
* `user:email`

Configure the token as `apiKey.vorto` in the Quarkus application configuration. For example, using the
environment variable `APIKEY_VORTO`:

~~~yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: vorto-converter
spec:
  template:
    …
    spec:
      containers:
        - image: ghcr.io/drogue-iot/vorto-converter:latest
          env:
            - name: APIKEY_VORTO
              valueFrom:
                secretKeyRef:
                  name: vorto-api
                  key: token
~~~

## Model caching

The application requires a mapping information model, each time it needs to map payload data. Fetching this model
from the model repository may take up to a few seconds. That is why the converter caches the information internally.
Knative, and serverless in general, is intended to scale down to zero. Which however means that the internal cache
cleared as well. That is why, in the above configuration, a "scale down to zero" of 5 minutes has been configured.

The ID of the cache is `mapping-cache`, and its default configuration can be tweaked as described in
[Quarkus - Application Data Caching](https://quarkus.io/guides/cache).
