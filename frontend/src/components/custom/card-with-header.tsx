import { cn } from '@/lib/utils'

export default function CardWithHeader({ children }: { children: React.ReactNode }) {
  return (
    <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow">
      {children}
    </div>
  )
}

CardWithHeader.Header = function CardHeader({
  children,
  className,
}: {
  children: React.ReactNode
  className?: string
}) {
  return <div className={cn('px-4 py-5 sm:px-6', className)}>{children}</div>
}

CardWithHeader.Body = function CardBody({
  children,
  className,
}: {
  children: React.ReactNode
  className?: string
}) {
  return <div className={cn('px-4 py-5 sm:p-6', className)}>{children}</div>
}
