{{/*
Expand the name of the chart.
*/}}
{{- define "credit-app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "credit-app.fullname" -}}
{{- if .Values.app.fullnameOverride }}
{{- .Values.app.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.app.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "credit-app.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "credit-app.labels" -}}
helm.sh/chart: {{ include "credit-app.chart" . }}
{{ include "credit-app.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "credit-app.selectorLabels" -}}
app.kubernetes.io/name: {{ include "credit-app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "credit-app.serviceAccountName" -}}
{{- if .Values.app.serviceAccount.create }}
{{- default (include "credit-app.fullname" .) .Values.app.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.app.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create image name
*/}}
{{- define "credit-app.image" -}}
{{- $tag := .Values.app.image.tag | default .Chart.AppVersion }}
{{- printf "%s:%s" .Values.app.image.repository $tag }}
{{- end }}

{{/*
Create database URL
*/}}
{{- define "credit-app.databaseUrl" -}}
{{- if .Values.mysql.enabled }}
{{- printf "jdbc:mysql://%s-mysql:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" .Release.Name .Values.mysql.auth.database }}
{{- else }}
{{- .Values.app.env.SPRING_DATASOURCE_URL }}
{{- end }}
{{- end }}

{{/*
Create database secret name
*/}}
{{- define "credit-app.databaseSecretName" -}}
{{- if .Values.mysql.enabled }}
{{- printf "%s-mysql-secret" .Release.Name }}
{{- else }}
{{- "creditapp-db-secret" }}
{{- end }}
{{- end }}