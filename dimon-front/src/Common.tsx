import { Component } from 'react';

export enum JobEventType {
    completed,
    completedinstantly,
    started,
    scheduled
}

export interface JobDTO {
    id: number,
    workerId: string,
    params: string,
    dueTo: string
}

export interface JobEvent {
    type: JobEventType,
    job: JobDTO
}