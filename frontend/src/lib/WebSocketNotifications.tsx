import useWebSocket, { ReadyState } from 'react-use-websocket'
import { ToastContainer, toast } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import clsx from 'clsx';
import { z } from 'zod';

function Toast({ title, description }: { title: string; description: string }) {
  return (
    <div>
      <span>{title}</span>
      <hr />
      <p className='text-sm'>{description}</p>
    </div>
  )
}

const NotificationSchema = z.object({
  track: z.object({
    id: z.number(),
    problemId: z.number(),
    problemName: z.string()
  }),
  message: z.string(),
  severity: z.enum(['NEUTRAL', 'SUCCESS', 'FAILURE'])
});
type Notification = z.infer<typeof NotificationSchema>

function NotificationToast({ notification }: { notification: Notification }) {
  return <Toast title={`${notification.track.problemName} (${notification.track.problemId})`} description={`${notification.message}`} />
}

function showNotification(notification: Notification) {
  console.log('New notification', notification)
  const toastItem = <NotificationToast notification={notification} />
  const props = { autoClose: 30000 }
  const severity = notification.severity
  if (severity === 'NEUTRAL') {
    toast.info(toastItem, props)
  } else if (severity === 'SUCCESS') {
    toast.success(toastItem, props)
  } else if (severity === 'FAILURE') {
    toast.error(toastItem, props)
  } else {
    console.warn(`unknown severity: ${severity}`)
  }
}

export function WebSocketNotifications() {
  const wsUrl = `${document.location.origin.replace(/^http/, 'ws')}/ws`
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
      const notification = NotificationSchema.parse(JSON.parse(e.data))
      showNotification(notification)
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
