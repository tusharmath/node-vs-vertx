import * as http from 'http'
import * as cluster from 'cluster'
import * as os from 'os'

const port = 8000
const numCPUs = 1 // os.cpus().length

if (cluster.isMaster) {
  for (let i = 0; i < numCPUs; i++) {
    const worker = cluster.fork()
    worker.on('exit', (code, signal) => {
      console.log(`worker ${worker.process.pid} died`)
    })
  }
} else {
  http
    .createServer((req, res) => {
      res.end('hello world\n')
    })
    .listen(port)

  console.log(`Worker ${process.pid} started ${port}`)
}
