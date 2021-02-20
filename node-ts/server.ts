import * as http from 'http'
import * as cluster from 'cluster'
import * as os from 'os'

const port = 8000

  http
    .createServer((req, res) => {
      res.end('hello world\n')
    })
    .listen(port)

  console.log(`server started ${port}`)

