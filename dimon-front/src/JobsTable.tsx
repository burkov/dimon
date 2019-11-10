import * as React from "react"
import { JobDTO } from './Common'
import { formatDistanceStrict } from 'date-fns'

interface JobEntryProps {
    jobs: JobDTO[],
    now: Date,
    name: string
}

export const JobsTable: React.SFC<JobEntryProps> = (props) => {
    if (props.jobs.length > 0) return <React.Fragment>
        <h1>{props.name}</h1>
        <table className="App">
            <tbody>
                {props.jobs.map(function (job) {
                    return <tr key={job.id}>
                        <td>{formatDistanceStrict(new Date(job.dueTo), props.now)} ago</td>
                        <td>{job.id}</td>
                        <td>{job.workerId}</td>
                        <td>{job.params}</td>
                    </tr>
                })}
            </tbody>
        </table>
    </React.Fragment>
    return <div />
}