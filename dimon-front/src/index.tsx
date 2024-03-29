import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import DispatcherMonitor from './DispatcherMonitor';
import * as serviceWorker from './serviceWorker';

ReactDOM.render(<DispatcherMonitor showNCompleted={32}/>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
