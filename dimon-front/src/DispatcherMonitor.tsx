import React, { Component } from 'react';
import './DispatcherMonitor.css';

import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
  RSocketClient,
  JsonSerializer,
  IdentitySerializer,
} from 'rsocket-core';
import { ISubscription } from 'rsocket-types'


type DispatcherMonitorState = {
  dataFromServer: Object
}

type DispatcherMonitorProps = {

}

enum JobEventType {
  insert,
  update,
  delete,
  snapshot,
  tablepoll
}

interface JobEvent {
  type: JobEventType
}

interface JobDTO {
  id: number,
  workerId: string,
  params: string,
  dueTo: string
}

interface JobEventInsert extends JobEvent {
  job: JobDTO
}

export default class DispatcherMonitor extends Component<{}, DispatcherMonitorState> {
  private totalEvents: number = 0;
  private totalReconnects: number = 0;
  constructor(props: DispatcherMonitorProps) {
    super(props);
    this.state = { dataFromServer: {} }
  }

  async componentDidMount() {
    this.connectRSocket();
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

  private handleInsert(data: JobEventInsert) {
    console.log(`${JSON.stringify(data.job)}`);
  }

  private handleJobEvent(data: JobEvent) {
    const type = JobEventType[data.type];
    this.setState({
      dataFromServer: {
        lastEventType: data.type,
        totalEvents: this.totalEvents,
        totalReconnects: this.totalReconnects
      }
    })
    console.log(`${data.type}, total events: ${this.totalEvents}, totalReconnects: ${this.totalReconnects}`);
    switch (+type) {
      case JobEventType.insert:
        this.handleInsert(data as JobEventInsert);
        break;
      case JobEventType.delete:
        // console.log(`${totalProcessed} DEL: ${data.jobId}`);
        break;
      case JobEventType.update:
        // console.log(`${totalProcessed} UPD: ${JSON.stringify(data.job)}`);
        break;
      case JobEventType.snapshot:
        // console.log(`SNP: ${data.jobs.length} items`)
        break;
      case JobEventType.tablepoll:
        break;
      default:
        console.error(`Unkown message: ${JSON.stringify(data)}`);
        break;
    }
  }

  render() {
    return <div className="App" >
      {JSON.stringify(this.state.dataFromServer)}
    </div>;
  }
}