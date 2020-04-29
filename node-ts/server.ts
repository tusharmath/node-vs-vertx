import * as http from 'http'
import * as cluster from 'cluster'
import * as os from 'os'

const port = 8000
// http
//   .createServer((req, res) => {
//     res.write('hello world\n')
//     res.end()
//   })
//   .listen(port)

// console.log(`Worker ${process.pid} started ${port}`)

const numCPUs = os.cpus().length / 4

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
