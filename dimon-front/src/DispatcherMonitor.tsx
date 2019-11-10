import React, { Component, Fragment } from 'react';
import './DispatcherMonitor.css';
import { JobDTO, JobEventType, JobEvent } from './Common'
import _ from 'lodash'


import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
  RSocketClient,
  JsonSerializer,
  IdentitySerializer,
} from 'rsocket-core';
import { ISubscription } from 'rsocket-types'
import { JobsTable } from './JobsTable';


type DispatcherMonitorState = {
  started: JobDTO[],
  completed: JobDTO[],
  scheduled: JobDTO[],
  now: Date
}

type DispatcherMonitorProps = {
  showNCompleted: number
}

export default class DispatcherMonitor extends Component<DispatcherMonitorProps, DispatcherMonitorState> {
  private totalEvents: number = 0;
  private totalReconnects: number = 0;
  constructor(props: DispatcherMonitorProps) {
    super(props);
    this.state = {
      now: new Date(),
      started: [],
      completed: [],
      scheduled: []
    }
  }

  async componentDidMount() {
    this.connectRSocket();
    setInterval(() => {
      this.setState(prevState => ({ ...prevState, now: new Date() }))
    }, 1000);
  }

  private connectRSocket() {
    const client = new RSocketClient({
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
      },
      setup: {
        keepAlive: 6000,
        lifetime: 60000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://localhost:8080/rsocket',
        debug: true,
      }),
    });
    client.connect().subscribe({
      onComplete: socket => {
        let requested = 0;
        let subscription: ISubscription;
        function requestMoreElementsIfNeeded() {
          if (--requested <= 0) {
            requested = 16;
            subscription.request(requested)
          }
        }
        socket.requestStream({
          metadata: `${String.fromCharCode('jobs-stream'.length)}jobs-stream`
        }).subscribe({
          onComplete: () => {
            console.error(`Stream sent onComplete. WTF?`);
          },
          onError: error => {
            console.log(`Got error: ${error}`);
            this.scheduleReconnect();
          },
          onNext: payload => {
            requestMoreElementsIfNeeded();
            this.totalEvents++;
            this.handleJobEvent(payload.data);
          },
          onSubscribe: s => {
            subscription = s;
            requestMoreElementsIfNeeded();
          }
        });
      },
      onError: error => {
        console.error(`Failed to subscribe. onError(${error})`);
        this.scheduleReconnect();
      },
      onSubscribe: _ => {
        console.log('Initializing. onSubscribe(cancelCallback) called');
      }
    });
  }

  private scheduleReconnect() {
    console.log('Reconnecting in few seconds...');
    setTimeout(() => {
      console.log("Reconnecting to RSocket");
      this.totalReconnects++;
      this.connectRSocket();
    }, 5000);
  }

  private handleScheduled(job: JobDTO) {
    this.setState({
      ...this.state,
      scheduled: [job, ...this.state.scheduled]
    });
  }

  private handleStarted(job: JobDTO) {
    this.setState({
      ...this.state,
      scheduled: this.removeJobById(this.state.scheduled, job.id),
      started: [job, ...this.state.started]
    });
  }

  private handleCompletion(job: JobDTO) {
    if (_.findIndex(this.state.completed, (e) => e.id === job.id) === -1) {
      this.setState({
        ...this.state,
        scheduled: this.removeJobById(this.state.scheduled, job.id),
        started: this.removeJobById(this.state.started, job.id),
        completed: _.take([job, ...this.state.completed], this.props.showNCompleted)
      });
    }
  }

  private removeJobById(jobs: JobDTO[], id: number) {
    return _.remove(jobs, (e) => e.id === id);
  }

  private handleJobEvent(data: JobEvent) {
    const type = JobEventType[data.type];
    switch (+type) {
      case JobEventType.completed:
      case JobEventType.completedinstantly:
        return this.handleCompletion(data.job);
      case JobEventType.scheduled:
        return this.handleScheduled(data.job);
      case JobEventType.started:
        return this.handleStarted(data.job);
      default:
        console.log(`unkonwn event: ${data}`)
    }
    console.log(`${type}, total events: ${this.totalEvents}, totalReconnects: ${this.totalReconnects}`);
  }

  render() {
    return <Fragment>
      <JobsTable name='Scheduled' jobs={this.state.scheduled} now={this.state.now} />
      <JobsTable name='Started' jobs={this.state.started} now={this.state.now} />
      <JobsTable name='Completed' jobs={this.state.completed} now={this.state.now} />
    </Fragment>;
  }
}