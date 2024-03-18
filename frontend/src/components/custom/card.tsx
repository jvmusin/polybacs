export default function Card({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-hidden rounded-lg bg-gray-50">
      <div className="px-4 py-5">{children}</div>
    </div>
  )
}
