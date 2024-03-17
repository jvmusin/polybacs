import { WebSocketNotifications } from '@/lib/WebSocketNotifications'
import { Outlet } from 'react-router-dom'

export default function Root() {
  return (
    <>
      <Layout />
      <WebSocketNotifications />
    </>
  )
}

// TODO: Add Inter font
function Layout() {
  return (
    <div className="flex h-screen min-w-[900px] flex-col text-xl">
      <Header />
      <main className="mx-auto w-full max-w-screen-xl flex-grow overflow-auto px-10">
        <div className="flex h-full gap-x-6">
          <Outlet />
        </div>
      </main>
      <Header />
    </div>
  )
}

function Header() {
  return (
    <div className="flex w-full items-center justify-center bg-gray-50 py-8">
      <h1 className="bg-gradient-to-r from-blue-400 to-pink-400 bg-clip-text text-4xl font-bold uppercase tracking-widest text-transparent">
        Polybacs
      </h1>
    </div>
  )
}
