import useWebSocket, { ReadyState } from 'react-use-websocket'
import { ToastContainer, toast } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import clsx from 'clsx';

function Toast({ title, description }: { title: string; description: string }) {
  return (
    <div>
      <span className="text-lg font-bold">{title}</span>
      <hr />
      <p>{description}</p>
    </div>
  )
}

export function WebSocketNotifications() {
  const wsUrl = document.location.origin.replace(/^http/, 'ws') + '/ws'
  const ws = useWebSocket(wsUrl, {
    onOpen: (e) => {
      console.log('Connected to server', e)
      toast.success(
        <Toast
          title="Connected to server"
          description="You are now connected to the server"
        />,
        { autoClose: 2000 },
      )
    },
    onClose: (e) => {
      console.log('Disconnected from server', e)
      toast.warn(
        <Toast
          title="Disconnected from server"
          description="You are now disconnected from the server"
        />,
        { autoClose: 2000 },
      )
    },
    onError: (e) => {
      console.error('An error occurred', e)
      toast(<Toast title="Error" description="An error occurred" />, {
        autoClose: 15000,
      })
    },
    onMessage: (e) => {
      console.log('New notification', e.data)
      toast.info(<Toast title="New notification" description={e.data} />, {
        autoClose: 30000,
      })
    },
    shouldReconnect: () => true,
  })

  const color = clsx(
    'absolute top-3 right-3 size-3 rounded-full', {
      'bg-yellow-500': ws.readyState === ReadyState.CONNECTING,
      'bg-green-500': ws.readyState === ReadyState.OPEN,
      'bg-red-500': ws.readyState === ReadyState.CLOSED,
    }
  )
  return (
    <div className="realtive">
      <div className={color} />
      <ToastContainer position="bottom-right" />
    </div>
  )
}
