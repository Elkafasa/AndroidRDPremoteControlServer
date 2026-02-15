SNDCPY – Android Remote Control Server

SNDCPY is a Wi-Fi based Android remote control server inspired by scrcpy.

This repository contains the Android server component responsible for:

Screen streaming

Audio streaming

Remote input injection

Secure pairing

Local network discovery

Telemetry reporting

The goal is to build a production-grade, low-latency remote control system designed with modular architecture and long-term scalability in mind.

Project Objectives

Low-latency video streaming

Secure authenticated remote control

Modular internal architecture

Clean separation of responsibilities

Production-ready foundation (not a prototype)

Deterministic build system

CI-ready repository

System Architecture

The server is divided into clearly separated domain modules:

app/src/main/java/com/example/remotecontrolserver/

server/ → Connection lifecycle & session management

streaming/ → Video & audio encoding pipeline

input/ → Touch / keyboard injection

security/ → Pairing & encryption

telemetry/ → Device metrics & system stats

Each module is designed to remain isolated and communicate through defined interfaces to prevent architectural drift.

High-Level System Flow

Server starts on Android device

Server advertises itself on local network (planned mDNS)

Client discovers and connects

Secure pairing handshake

Encrypted session established

Continuous streaming + input loop:

Screen frames encoded (MediaCodec → H.264)

Audio captured (AudioRecord → AAC)

Input events injected

Telemetry data streamed

Networking Model

Initial Transport:

TCP sockets

Future:

QUIC

Multiplexed framed transport

Logical Channels:

Control → Session + handshake

Video → H.264 encoded frames

Audio → AAC encoded audio

Input → Remote touch / keyboard events

Telemetry → JSON system metrics

Future upgrade:

Binary protocol

Frame-based message envelope

Back-pressure handling

Security Model

Security is mandatory.

Planned model:

Pairing-based trust establishment

Ephemeral session keys

AES-GCM encrypted transport

Device identity persistence

No unauthenticated remote control

Future enhancements:

Key rotation

Mutual authentication

Session resume tokens

Streaming Pipeline (Planned Implementation)

Video pipeline:

Surface
→ MediaCodec (H.264)
→ Packetizer
→ Socket transport

Audio pipeline:

AudioRecord
→ AAC encoder
→ Packetizer
→ Socket transport

Planned improvements:

Hardware encoder preference

Adaptive bitrate

Frame skipping under load

Delta encoding optimization

Remote Input Injection

Planned injection mechanisms:

AccessibilityService

Instrumentation injection

Native input bridge (future advanced mode)

Supported inputs:

Multi-touch gestures

Swipe

Scroll

Keyboard

System buttons

Special keys

Input injection is restricted to authenticated sessions only.

Telemetry

The telemetry module provides:

Battery level

CPU temperature

Memory usage

Network stats

Device performance indicators

Telemetry is streamed independently from control data.

Version Roadmap

0.1.0

Clean architecture skeleton

Deterministic Gradle build

Module separation

CI-ready foundation

0.2.0

Basic TCP server

Local discovery prototype

Session establishment

0.3.0

Video streaming prototype

Basic input injection

0.5.0

Secure pairing layer

Encrypted session transport

1.0.0

Production-grade low-latency remote control server

Build Instructions

Run:

./gradlew assembleDebug

Requirements:

JDK 17+

Android SDK (compileSdk 34)

Repository Structure

SNDCPY/

app/

gradle/

gradlew

gradlew.bat

settings.gradle

build.gradle

gradle.properties

The repository includes a fully functional Gradle wrapper (8.7).

Current Status

Early-stage architectural foundation complete.

The system is structured for production scalability. Core streaming and network implementation are the next development phase.

Design Philosophy

This project prioritizes:

Explicit architecture

Clean module boundaries

Security-first design

Deterministic builds

Long-term maintainability