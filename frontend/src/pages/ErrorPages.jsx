import { Link } from 'react-router-dom';
import { Home, AlertTriangle } from 'lucide-react';
import './ErrorPages.css';

export function NotFoundPage() {
  return (
    <div className="error-page">
      <div className="error-content animate-fade-in">
        <div className="error-code">404</div>
        <h2>Trang không tồn tại</h2>
        <p>Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <Link to="/dashboard" className="btn btn-primary" id="btn-go-home">
          <Home size={18} /> Về trang chủ
        </Link>
      </div>
    </div>
  );
}

export function ErrorPage({ error, resetError }) {
  return (
    <div className="error-page">
      <div className="error-content animate-fade-in">
        <div className="error-icon"><AlertTriangle size={48} /></div>
        <h2>Đã có lỗi xảy ra</h2>
        <p>{error?.message || 'Ứng dụng gặp sự cố. Vui lòng thử lại.'}</p>
        <div style={{ display: 'flex', gap: 'var(--space-md)' }}>
          <button className="btn btn-secondary" onClick={resetError}>Thử lại</button>
          <Link to="/dashboard" className="btn btn-primary" id="btn-home-on-error"><Home size={18} /> Trang chủ</Link>
        </div>
      </div>
    </div>
  );
}

export class ErrorBoundary extends Error {
  constructor(props) { super(props); this.state = { error: null }; }
  static getDerivedStateFromError(error) { return { error }; }
  render() {
    if (this.state.error) {
      return <ErrorPage error={this.state.error} resetError={() => this.setState({ error: null })} />;
    }
    return this.props.children;
  }
}
